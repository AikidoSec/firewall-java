package dev.aikido.agent_api;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetUser {
    private static final Logger logger = LogManager.getLogger(SetUser.class);

    public record UserObject(String id, String name) {}
    public static void setUser(UserObject user) {
        if(user.id().isEmpty() || user.name().isEmpty()) {
            logger.info("User ID or name cannot be empty.");
            return;
        }
        ContextObject currentContext = Context.get();
        if (currentContext == null) {
            return;
        }
        if (currentContext.middlewareExecuted()) {
            logger.info(
                "setUser(...) must be called before the Zen middleware is executed."
            );
        }
        User validatedUser = new User(user.id(), user.name(), currentContext.getRemoteAddress());
        currentContext.setUser(validatedUser);
        Context.set(currentContext);
    }
}
