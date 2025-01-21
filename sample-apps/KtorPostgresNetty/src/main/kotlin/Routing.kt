package com.example

import com.example.database.DatabaseHelper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respondText("OK!")
        }

        // Pages :
        get("/") {
            call.respondFile(File("src/main/resources/static/index.html"))
        }
        get("/pages/create") {
            call.respondFile(File("src/main/resources/static/create.html"))
        }
        get("/pages/execute") {
            call.respondFile(File("src/main/resources/static/execute_command.html"))
        }
        get("/pages/request") {
            call.respondFile(File("src/main/resources/static/request.html"))
        }
        get("/pages/read") {
            call.respondFile(File("src/main/resources/static/read_file.html"))
        }

        // Serve API
        get("/api/pets/") {
            val pets = DatabaseHelper.getAllPets()
            call.respond(pets)
        }

        post("/api/create") {
            val createRequest = call.receive<CreateRequest>()
            val petName = createRequest.name
            val rowsCreated = DatabaseHelper.createPetByName(petName)
            call.respondText("Rows created: $rowsCreated", status = HttpStatusCode.Created)
        }

        post("/api/execute") {
            val commandRequest = call.receive<CommandRequest>()
            val userCommand = commandRequest.userCommand
            val result = executeShellCommand(userCommand)
            call.respondText(result)
        }

        get("/api/execute/{command}") {
            val userCommand = call.parameters["command"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val result = executeShellCommand(userCommand)
            call.respondText(result)
        }

        post("/api/request") {
            val requestRequest = call.receive<RequestRequest>()
            val url = requestRequest.url
            val response = makeHttpRequest(url)
            call.respondText(response)
        }

        get("/api/read") {
            val filePath = call.request.queryParameters["path"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val content = readFile(filePath)
            call.respondText(content)
        }
    }
}

@Serializable
data class CreateRequest(val name: String)

@Serializable
data class CommandRequest(val userCommand: String)

@Serializable
data class RequestRequest(val url: String)

