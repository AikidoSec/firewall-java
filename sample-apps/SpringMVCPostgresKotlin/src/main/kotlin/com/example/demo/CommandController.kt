package com.example.demo

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.io.IOException

@RestController
@RequestMapping("/api/commands") // Base URL for all routes in this controller
class CommandController {

    data class CommandsExecute(val userCommand: String)

    @PostMapping(
        path = ["/execute"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Throws(IOException::class, InterruptedException::class)
    fun executeCommand(@RequestBody commandData: CommandsExecute): String {
        val command = commandData.userCommand
        println("Executing command: $command")

        val process = Runtime.getRuntime().exec(command)

        return process.waitFor().toString()
    }

    @GetMapping(path = ["/execute/{command_name}"])
    @Throws(IOException::class, InterruptedException::class)
    fun executeCommand(@PathVariable("command_name") commandName: String): String {
        println("Executing command: $commandName")

        val process = Runtime.getRuntime()
            .exec(StringBuilder().append("echo '").append(commandName).append("'").toString())

        return process.waitFor().toString()
    }
}
