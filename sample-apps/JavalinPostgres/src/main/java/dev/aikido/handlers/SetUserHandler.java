package dev.aikido.handlers;

import dev.aikido.agent_api.SetUser;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import static dev.aikido.agent_api.SetUser.setUser;

public class SetUserHandler implements Handler {
    @Override
    public void handle(Context ctx) throws Exception {
        String userId = ctx.header("user");
        if (userId != null) {
            // Useful for end-2-end tests:
            setUser(new SetUser.UserObject(userId, "John Doe"));
        }
    }
}