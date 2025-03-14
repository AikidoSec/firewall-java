package dev.aikido.SpringWebfluxSampleApp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/api/commands") // Base URL for all routes in this controller
public class CommandController {
    private record CommandsExecute(String userCommand) {}

    @GetMapping(path = "/execute/{userCommand}")
    public Mono<String> executeCommand(@PathVariable String userCommand) {
        return executeCommandInternal(userCommand);
    }

    @GetMapping(path = "/executeFromCookie")
    public Mono<String> executeCommandFromCookie(@CookieValue("command") String command) {
        if (command == null) {
            throw new RuntimeException("Command cannot be empty");
        }
        return executeCommandInternal(command);
    }

    private Mono<String> executeCommandInternal(String command) {
        System.out.println("Executing command: " + command);

        return Mono.fromCallable(() -> {
            Process process = Runtime.getRuntime().exec("echo " + command);
            StringBuilder output = new StringBuilder();

            // Read the output of the command
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for the process to complete and return the output
            process.waitFor();
            return output.toString();
        });
    }
}