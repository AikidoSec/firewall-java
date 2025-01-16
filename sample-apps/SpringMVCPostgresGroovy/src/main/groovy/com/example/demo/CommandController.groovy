package com.example.demo

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/commands") // Base URL for all routes in this controller
class CommandController {

     static class CommandsExecute {
        String userCommand
    }

    @PostMapping(path = "/execute",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    String execute(@RequestBody CommandsExecute commandData) throws IOException, InterruptedException {
        String command = commandData.userCommand
        println("Executing command: " + command)

        Process process = Runtime.getRuntime().exec(command)

        return String.valueOf(process.waitFor())
    }

    @GetMapping(path = "/execute/{command_name}")
    String execute(@PathVariable("command_name") String commandName) throws IOException, InterruptedException {
        println("Executing command: " + commandName)

        Process process = Runtime.getRuntime().exec(commandName)

        return String.valueOf(process.waitFor())
    }
}
