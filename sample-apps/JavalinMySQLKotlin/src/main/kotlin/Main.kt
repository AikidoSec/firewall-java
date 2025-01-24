import io.javalin.Javalin

fun main() { // can omit args
    val app = Javalin.create().start(7070)
    app.get("/") { ctx -> ctx.result("Hello World") }
}