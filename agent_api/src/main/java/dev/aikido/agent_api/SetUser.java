package dev.aikido.agent_api;

import dev.aikido.agent_api.background.ipc_commands.RegisterUserCommand;
import dev.aikido.agent_api.background.utilities.ThreadClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static dev.aikido.agent_api.background.utilities.ThreadClientFactory.getDefaultThreadClient;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public final class SetUser {
    private SetUser() {}
    private static final Logger logger = LogManager.getLogger(SetUser.class);

    public record UserObject(String id, String name) {}
    public static void setUser(UserObject user) {
        if(user.id() == null || user.id().isEmpty() || user.name() == null || user.name().isEmpty()) {
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
        long time = getUnixTimeMS();
        User validatedUser = new User(user.id(), user.name(), currentContext.getRemoteAddress(), time);
        // Update context:
        currentContext.setUser(validatedUser);
        Context.set(currentContext);

        // Register user (Send to cloud)
        ThreadClient threadClient = getDefaultThreadClient();
        if (threadClient != null) {
            new RegisterUserCommand().send(threadClient, validatedUser);
        }
    }
}
