package com.example.demo.resources;

import org.restlet.data.MediaType;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandResources extends ServerResource {

    private static class CommandsExecute {
        public String userCommand;

        // Default constructor is needed for JSON deserialization
        public CommandsExecute() {}

        public CommandsExecute(String userCommand) {
            this.userCommand = userCommand;
        }
    }

    @Post("json")
    public String execute(String jsonInput) throws IOException, InterruptedException {
        CommandsExecute commandData = parseJson(jsonInput);
        String command = commandData.userCommand;
        System.out.println("Executing command: " + command);

        Process process = Runtime.getRuntime().exec(command);
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        output.append("Exit code: ").append(exitCode);
        return output.toString();
    }

    private CommandsExecute parseJson(String jsonInput) {
        // Simple JSON parsing (you can use a library like Jackson or Gson for more complex cases)
        String command = jsonInput.replaceAll(".*\"userCommand\":\"([^\"]+)\".*", "$1");
        return new CommandsExecute(command);
    }
}
