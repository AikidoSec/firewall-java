package dev.aikido.agent.wrappers.micronaut;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.MicronautAnnotationCollector;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.MicronautContextObject;
import dev.aikido.agent_api.storage.RequestLocalStorage;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.exceptions.HttpStatusException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Wraps Micronaut controller methods (@Get/@Post/@Put/@Delete/@Patch) and @RequestFilter methods.
 * A @RequestFilter invocation builds the context and stashes it in RequestLocalStorage keyed by the
 * request (which survives the thread hop to a virtual/blocking controller, unlike our ThreadLocal);
 * the controller re-attaches it. Inbound IP/UA/allowlist blocking is enforced on both, so requests
 * that never reach a controller are covered; rate-limiting and user-blocking are left to a user
 * @ServerFilter (see docs/micronaut.md).
 */
public class MicronautControllerWrapper implements Wrapper {
    public String getName() {
        return MicronautAdvice.class.getName();
    }

    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isAnnotatedWith(
                nameContainsIgnoreCase("io.micronaut.http.annotation")
                        .and(nameContainsIgnoreCase("Get")
                                .or(nameContainsIgnoreCase("Post"))
                                .or(nameContainsIgnoreCase("Put"))
                                .or(nameContainsIgnoreCase("Delete"))
                                .or(nameContainsIgnoreCase("Patch"))
                                .or(nameContainsIgnoreCase("RequestFilter"))));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return hasSuperType(declaresMethod(getMatcher()));
    }

    public static class MicronautAdvice {
        // Public so inlined Advice code in another package can call it without IllegalAccessError.
        // Matched by annotation name, not class reference: @RequestFilter doesn't exist in the
        // oldest Micronaut version this module compiles against (3.10.4).
        public static boolean isRouteMethod(Executable method) {
            for (java.lang.annotation.Annotation annotation : method.getAnnotations()) {
                if ("io.micronaut.http.annotation.RequestFilter".equals(annotation.annotationType().getName())) {
                    return false;
                }
            }
            return true;
        }

        public static ContextObject buildContextObject(HttpRequest<?> req) {
            String rawIp = null;
            if (req.getRemoteAddress() != null && req.getRemoteAddress().getAddress() != null) {
                rawIp = req.getRemoteAddress().getAddress().getHostAddress();
            }
            Map<String, List<String>> query = new HashMap<>(req.getParameters().asMap());
            Map<String, List<String>> headers = new HashMap<>(req.getHeaders().asMap());
            HashMap<String, List<String>> cookies = new HashMap<>();
            for (Cookie cookie : req.getCookies().getAll()) {
                cookies.computeIfAbsent(cookie.getName(), k -> new ArrayList<>()).add(cookie.getValue());
            }
            return new MicronautContextObject(
                    req.getMethodName(), req.getUri().toString(), rawIp, query, cookies, headers);
        }

        // Establishes the request context on this thread: re-attaches one built earlier (maybe on
        // another thread), or builds a fresh one and runs inbound checks, remembering both in
        // RequestLocalStorage so a later thread-hop can reuse them.
        public static void establishContext(HttpRequest<?> req) {
            ContextObject reused = (ContextObject) RequestLocalStorage.getContext(req);
            if (reused != null) {
                Context.set(reused);
                return;
            }
            WebRequestCollector.Res freshBlock = WebRequestCollector.report(buildContextObject(req));
            if (Context.get() == null) {
                return; // IP-bypassed: report chose not to set a context, so nothing to remember.
            }
            RequestLocalStorage.setContext(req, Context.get());
            if (freshBlock != null) {
                RequestLocalStorage.setBlock(req, freshBlock);
            }
        }

        @Advice.OnMethodEnter
        public static void before(
                @Advice.Origin Executable method,
                @Advice.AllArguments(readOnly = true, typing = DYNAMIC) Object[] args) {
            WebRequestCollector.Res block = null;
            try {
                HttpRequest<?> req = ServerRequestContext.currentRequest().orElse(null);
                boolean isRoute = isRouteMethod(method);
                if (Context.get() == null && req != null) {
                    establishContext(req);
                }
                block = req != null ? (WebRequestCollector.Res) RequestLocalStorage.getBlock(req) : null;
                // A @RequestFilter runs before binding for every request, so enforcing the block here
                // too covers requests that never reach a controller (binding errors, short-circuits).
                if (isRoute && block == null) {
                    MicronautAnnotationCollector.report(method.getParameters(), args);
                }
            } catch (Throwable ignored) {
                // never let instrumentation break the application
            }
            if (block != null) {
                Context.reset(); // clear before we abort so the thread does not leak context
                // The (status, Object body) ctor makes Micronaut write the body as-is instead of
                // wrapping it in its default JSON error envelope.
                throw new HttpStatusException(HttpStatus.valueOf(block.status()), (Object) block.msg());
            }
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void after(
                @Advice.Origin Executable method,
                @Advice.Return(typing = DYNAMIC, readOnly = true) Object returned,
                @Advice.Thrown Throwable thrown) {
            if (!isRouteMethod(method)) {
                Context.reset(); // no response to report yet, but this thread must not leak context
                return;
            }
            HttpRequest<?> req = ServerRequestContext.currentRequest().orElse(null);
            try {
                if (Context.get() != null) {
                    int status = 200;
                    if (thrown != null) {
                        status = 500;
                    } else if (returned instanceof HttpResponse) {
                        status = ((HttpResponse<?>) returned).code();
                    }
                    WebResponseCollector.report(status);
                }
            } finally {
                Context.reset(); // clear so a reused thread does not leak context to the next request
                if (req != null) {
                    RequestLocalStorage.remove(req); // request is done: drop its stored context/block
                }
            }
        }
    }
}
