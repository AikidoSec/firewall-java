package handlers

import dev.aikido.agent_api.SetUser
import dev.aikido.agent_api.SetUser.setUser
import io.javalin.http.Context
import io.javalin.http.Handler

class SetUserHandler : Handler {
    @Throws(Exception::class)
    override fun handle(ctx: Context) {
        val userId = ctx.header("user")
        if (userId != null) {
            // Useful for end-2-end tests:
            setUser(SetUser.UserObject(userId, "John Doe"))
        }
    }
}