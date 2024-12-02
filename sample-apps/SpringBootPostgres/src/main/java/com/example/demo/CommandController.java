package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

@RestController
@RequestMapping("/api/commands") // Base URL for all routes in this controller
public class CommandController {
    private record CommandsExecute(String userCommand) {}
    @PostMapping(path = "/execute",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String read(@RequestBody CommandsExecute commandData) throws IOException, InterruptedException {
        String command = commandData.userCommand;
        System.out.println("Executing command: "+ command);

        Process process = Runtime.getRuntime()
                .exec(command);

        return String.valueOf(process.waitFor());
    }

    @GetMapping(path = "/execute/{command_name}")
    public String read(@PathVariable("command_name") String commandName) throws IOException, InterruptedException {
        System.out.println("Executing command: "+ commandName);

        Process process = Runtime.getRuntime()
                .exec(commandName);

        return String.valueOf(process.waitFor());
    }
}