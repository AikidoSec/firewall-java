import io.javalin.Javalin
import io.javalin.http.Context

fun main() {
    val app = Javalin.create().start(7070)
    
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
}