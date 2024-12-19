package dev.aikido;

import dev.aikido.agent_api.SetUser;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class SetUserMiddleware implements Handler {

    @Override
    public void handle(Context ctx) throws Exception {
        // Set user information (you can customize this as needed)
        SetUser.setUser(new SetUser.UserObject("123", "John Doe"));
    }
}