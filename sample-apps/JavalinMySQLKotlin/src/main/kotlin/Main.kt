import DatabaseHelper.allPets
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dev.aikido.agent_api.ShouldBlockRequest
import handlers.SetUserHandler
import io.javalin.Javalin
import io.javalin.http.Context

fun main() {
    val port = if (System.getenv("PORT") != null) Integer.parseInt(System.getenv("PORT")) else 7070;
    val app = Javalin.create().start(port)

    // Handlers :
    app.before(SetUserHandler())
    app.before { ctx: Context ->
        val shouldBlockRequestResult = ShouldBlockRequest.shouldBlockRequest()
        if (shouldBlockRequestResult.block()) {
            if (shouldBlockRequestResult.data().type() == "ratelimited") {
                var message = "You are rate limited by Zen."
                if (shouldBlockRequestResult.data().trigger() == "ip") {
                    message = message + " (Your IP: " + shouldBlockRequestResult.data().ip() + ")"
                }

                setResponse(ctx, message, 429)
            } else if (shouldBlockRequestResult.data().type() == "blocked") {
                setResponse(ctx, "You are blocked by Zen.", 403)
            }
        }
    }

    // Static content :
    app.get("/") { ctx: Context ->
        ctx.html(loadHtmlFromFile("src/main/resources/index.html"))
    }
    app.get("/pages/execute") { ctx: Context ->
        ctx.html(loadHtmlFromFile("src/main/resources/execute_command.html"))
    }
    app.get("/pages/create") { ctx: Context ->
        ctx.html(loadHtmlFromFile("src/main/resources/create.html"))
    }
    app.get("/pages/request") { ctx: Context ->
        ctx.html(loadHtmlFromFile("src/main/resources/request.html"))
    }
    app.get("/pages/read") { ctx: Context ->
        ctx.html(loadHtmlFromFile("src/main/resources/read_file.html"))
    }

    // Test rate-limiting :
    app.get("/test_ratelimiting_1") { ctx: Context ->
        ctx.result("OK")
    }


    // Serve API :
    app.get("/api/pets") { ctx: Context ->
        ctx.json(allPets)
    }

    app.post("/api/create") { ctx: Context ->
        val petName: String = ctx.bodyAsClass(CreateRequest::class.java).name
        ctx.result(petName)
        val rowsCreated = DatabaseHelper.createPetByName(petName)
        ctx.result(rowsCreated.toString())
    }

    app.post("/api/execute") { ctx: Context ->
        val userCommand: String = ctx.bodyAsClass(CommandRequest::class.java).userCommand
        val result = executeShellCommand(userCommand)
        ctx.result(result)
    }
    app.get("/api/execute/<command>") { ctx: Context ->
        val userCommand = ctx.pathParam("command")
        val result = executeShellCommand(userCommand)
        ctx.result(result)
    }

    app.post("/api/request") { ctx: Context ->
        val url: String = ctx.bodyAsClass(RequestRequest::class.java).url
        val response = makeHttpRequest(url)
        ctx.result(response)
    }
    app.get("/api/read") { ctx: Context ->
        val filePath = ctx.queryParam("path")
        val content: String = readFile(filePath)
        ctx.result(content)
    }
    app.post("/api/read") { ctx: Context ->
        val filePath = ctx.bodyAsClass(FileRequest::class.java).fileName
        val content: String = readFile(filePath)
        ctx.result(content)
    }
}

data class CommandRequest @JsonCreator constructor(
    @JsonProperty("userCommand") val userCommand: String
)
data class RequestRequest @JsonCreator constructor(
    @JsonProperty("url") val url: String
)
data class FileRequest @JsonCreator constructor(
    @JsonProperty("fileName") val fileName: String
)
data class CreateRequest @JsonCreator constructor(
    @JsonProperty("name") val name: String
)

fun setResponse(ctx: Context, text: String, statusCode: Int) {
    ctx.status(statusCode)
    ctx.contentType("text/plain")
    ctx.result(text)
    ctx.skipRemainingHandlers()
}