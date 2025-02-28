package dev.aikido;

import dev.aikido.agent_api.middleware.AikidoJavalinMiddleware;
import dev.aikido.handlers.SetUserHandler;
import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static dev.aikido.Helpers.*;

public class JavalinPostgres {
    public static class CommandRequest { public String userCommand;}
    public static class RequestRequest { public String url;}
    public static class CreateRequest { public String name;}

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(Integer.valueOf(System.getProperty("portNumber", "8088")));
        // Add our middleware :
        app.before(new SetUserHandler());
        app.before(new AikidoJavalinMiddleware());

        app.get("/", ctx -> {
            ctx.html(loadHtmlFromFile("src/main/resources/index.html"));
        });
        app.get("/pages/execute", ctx -> {
            ctx.html(loadHtmlFromFile("src/main/resources/execute_command.html"));
        });
        app.get("/pages/create", ctx -> {
            ctx.html(loadHtmlFromFile("src/main/resources/create.html"));
        });
        app.get("/pages/request", ctx -> {
            ctx.html(loadHtmlFromFile("src/main/resources/request.html"));
        });
        app.get("/pages/read", ctx -> {
            ctx.html(loadHtmlFromFile("src/main/resources/read_file.html"));
        });

        // Test rate-limiting :
        app.get("/test_ratelimiting_1", ctx -> {
            ctx.result("OK");
        });

        // Path parameters :
        app.get("/hello/{name}", ctx -> { // the {} syntax does not allow slashes ('/') as part of the parameter
            ctx.result("Hello: " + ctx.pathParam("name"));
        });
        app.get("/hello/<name>", ctx -> { // the <> syntax allows slashes ('/') as part of the parameter
            ctx.result("Hello: " + ctx.pathParam("name"));
        });

        // Serve API :
        app.get("/api/pets", ctx -> {
            ArrayList<Object> pets = DatabaseHelper.getAllPets();
            ctx.json(pets);
        });

        app.post("/api/create", ctx -> {
            String petName = ctx.bodyAsClass(CreateRequest.class).name;
            ctx.result(petName);
            Integer rowsCreated = DatabaseHelper.createPetByName(petName);
            ctx.result(String.valueOf(rowsCreated));
        });

        app.post("/api/execute", ctx -> {
            String userCommand = ctx.bodyAsClass(CommandRequest.class).userCommand;
            String result = executeShellCommand(userCommand);
            ctx.result(result);
        });
        app.get("/api/execute/<command>", ctx -> {
            String userCommand = ctx.pathParam("command");
            String result = executeShellCommand(userCommand);
            ctx.result(result);
        });

        app.post("/api/request", ctx -> {
            String url = ctx.bodyAsClass(RequestRequest.class).url;
            String response = makeHttpRequest(url);
            ctx.result(response);
        });

        app.post("/api/request2", ctx -> {
            String url = ctx.bodyAsClass(RequestRequest.class).url;
            String response = makeHttpRequestWithOkHttp(url);
            ctx.result(response);
        });

        app.get("/api/read", ctx -> {
            String filePath = ctx.queryParam("path");
            String content = Helpers.readFile(filePath);
            ctx.result(content);
        });
    }

    private static String loadHtmlFromFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Error loading HTML file: " + e.getMessage();
        }
        return content.toString();
    }
}