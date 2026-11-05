package com.example.probe;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.RequestBean;
import io.micronaut.http.context.ServerRequestContext;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Each endpoint logs the thread at: filter (see ThreadProbeFilter) -> controller -> sink (JDBC).
 * If all stages share one thread, a ThreadLocal-based context survives. If they differ, it hops.
 */
@Controller("/probe")
public class ProbeController {

    private final PetRepository repo;

    public ProbeController(PetRepository repo) {
        this.repo = repo;
    }

    private String id(HttpRequest<?> req) {
        return req.getAttribute("probeId", String.class).orElse("?");
    }

    /** Case A: explicit blocking offload (the recommended pattern for JDBC). */
    @Get("/blocking-execon")
    @ExecuteOn(TaskExecutors.BLOCKING)
    public Map<String, Object> blockingExecOn(HttpRequest<?> req) {
        String id = id(req);
        ProbeContext.CTX.set(id); // simulate Zen establishing context ON the controller thread
        boolean reqPresent = ServerRequestContext.currentRequest().isPresent();
        boolean getVisible = false;
        try {
            getVisible = ProbeController.class.getMethod("blockingExecOn", HttpRequest.class)
                    .isAnnotationPresent(io.micronaut.http.annotation.Get.class);
        } catch (NoSuchMethodException ignored) {}
        ThreadLog.log(id, "2-controller (blocking-execon) ServerRequestContext.present=" + reqPresent + " @Get-reflection-visible=" + getVisible);
        long count = repo.count(); // real JDBC call
        ThreadLog.log(id, "3-sink JDBC done, count=" + count);
        return Map.of("endpoint", "blocking-execon", "controllerThread", Thread.currentThread().getName());
    }

    /** SQLi via query param (@QueryValue). Runs on the event loop (no @ExecuteOn). */
    @Get("/sqli")
    public Map<String, Object> sqli(HttpRequest<?> req, @QueryValue(value = "name", defaultValue = "") String name) {
        String id = id(req);
        ThreadLog.log(id, "2-controller (query-sqli) name=" + name);
        return runQuery(id, "query-sqli", name);
    }

    /** SQLi via path variable (@PathVariable). */
    @Get("/path-sqli/{name}")
    public Map<String, Object> pathSqli(HttpRequest<?> req, @PathVariable String name) {
        String id = id(req);
        ThreadLog.log(id, "2-controller (path-sqli) name=" + name);
        return runQuery(id, "path-sqli", name);
    }

    /** SQLi via POST @Body (JSON -> Map). */
    @Post("/body-sqli")
    public Map<String, Object> bodySqli(HttpRequest<?> req, @Body Map<String, Object> payload) {
        String id = id(req);
        String name = String.valueOf(payload.getOrDefault("name", ""));
        ThreadLog.log(id, "2-controller (body-sqli) name=" + name);
        return runQuery(id, "body-sqli", name);
    }

    /** SQLi on a BLOCKING controller (@ExecuteOn) -> runs on a virtual thread, not the event loop. */
    @Get("/blocking-sqli")
    @ExecuteOn(TaskExecutors.BLOCKING)
    public Map<String, Object> blockingSqli(HttpRequest<?> req, @QueryValue(value = "name", defaultValue = "") String name) {
        String id = id(req);
        ThreadLog.log(id, "2-controller (blocking-sqli) thread=" + Thread.currentThread().getName() + " name=" + name);
        return runQuery(id, "blocking-sqli", name);
    }

    /** SQLi via POST @Body bound to a POJO (no HttpRequest param -> uses ServerRequestContext). */
    @Post("/body-pojo")
    public Map<String, Object> bodyPojo(@Body SearchRequest body) {
        return runQuery("pojo", "body-pojo", body.name());
    }

    /** SQLi via @RequestBean (fields aggregated into a bean). */
    @Get("/reqbean")
    public Map<String, Object> reqbean(@RequestBean SearchBean bean) {
        return runQuery("bean", "reqbean", bean.name());
    }

    /** SQLi via PUT. */
    @Put("/put-sqli")
    public Map<String, Object> putSqli(@QueryValue(value = "name", defaultValue = "") String name) {
        return runQuery("put", "put-sqli", name);
    }

    /** SQLi via DELETE. */
    @Delete("/delete-sqli")
    public Map<String, Object> deleteSqli(@QueryValue(value = "name", defaultValue = "") String name) {
        return runQuery("del", "delete-sqli", name);
    }

    /** Multiple path variables; SQLi payload can be in either. */
    @Get("/multi/{a}/{b}")
    public Map<String, Object> multi(@PathVariable String a, @PathVariable String b) {
        return runQuery("multi", "multi", a + b);
    }

    /** Command injection sink (Runtime.exec via /bin/sh). */
    @Get("/cmdi")
    public Map<String, Object> cmdi(@QueryValue(value = "name", defaultValue = "") String name) {
        try {
            Process p = Runtime.getRuntime().exec("ls " + name); // exec(String) form Zen wraps
            p.waitFor();
            return Map.of("endpoint", "cmdi", "exit", p.exitValue());
        } catch (Exception e) {
            return Map.of("endpoint", "cmdi", "blocked", true, "error", e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /** Path traversal sink (new File). */
    @Get("/pathtraversal")
    public Map<String, Object> pathtraversal(@QueryValue(value = "name", defaultValue = "") String name) {
        try {
            File f = new File("/tmp/zenprobe/" + name);
            return Map.of("endpoint", "pathtraversal", "exists", f.exists(), "path", f.getPath());
        } catch (Exception e) {
            return Map.of("endpoint", "pathtraversal", "blocked", true, "error", e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private Map<String, Object> runQuery(String id, String endpoint, String name) {
        List<String> rows = new ArrayList<>();
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:zenprobe", "SA", "")) {
            Statement st = c.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS pet(name VARCHAR(255))");
            String sql = "SELECT name FROM pet WHERE name = '" + name + "'"; // deliberate SQLi sink
            ThreadLog.log(id, "3-sink SQL = " + sql);
            try (ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) rows.add(rs.getString(1));
            }
        } catch (Exception e) {
            ThreadLog.log(id, "3-sink EXCEPTION " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return Map.of("endpoint", endpoint, "blocked", true, "error", e.getClass().getName() + ": " + e.getMessage());
        }
        return Map.of("endpoint", endpoint, "rows", rows, "count", rows.size());
    }

    /** Case B: default controller, no @ExecuteOn, returns a plain value + does JDBC. */
    @Get("/default")
    public Map<String, Object> defaultController(HttpRequest<?> req) {
        String id = id(req);
        ThreadLog.log(id, "2-controller (default / no-annotation)");
        long count = repo.count(); // real JDBC call
        ThreadLog.log(id, "3-sink JDBC done, count=" + count);
        return Map.of("endpoint", "default", "controllerThread", Thread.currentThread().getName());
    }

    /** Case C: reactive controller returning Mono, with an explicit scheduler hop. */
    @Get("/reactive")
    public Mono<Map<String, Object>> reactive(HttpRequest<?> req) {
        String id = id(req);
        ThreadLog.log(id, "2-controller (reactive) assembly");
        return Mono.fromCallable(() -> {
                    ThreadLog.log(id, "3-sink inside fromCallable");
                    return repo.count();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(count -> {
                    ThreadLog.log(id, "4-map after sink");
                    return Map.<String, Object>of("endpoint", "reactive", "mapThread", Thread.currentThread().getName());
                });
    }

    /** Case D: reactive controller with NO explicit scheduler — does it still hop? */
    @Get("/reactive-nohop")
    public Mono<Map<String, Object>> reactiveNoHop(HttpRequest<?> req) {
        String id = id(req);
        ThreadLog.log(id, "2-controller (reactive-nohop) assembly");
        return Mono.fromCallable(() -> {
                    ThreadLog.log(id, "3-sink inside fromCallable");
                    return repo.count();
                })
                .map(count -> {
                    ThreadLog.log(id, "4-map after sink");
                    return Map.<String, Object>of("endpoint", "reactive-nohop", "mapThread", Thread.currentThread().getName());
                });
    }
}
