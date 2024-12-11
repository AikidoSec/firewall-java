package dev.aikido.agent_api;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.utilities.IPCClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static dev.aikido.agent_api.background.utilities.IPCClientFactory.getDefaultIPCClient;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public final class SetUser {
    private SetUser() {}
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
        long time = getUnixTimeMS();
        User validatedUser = new User(user.id(), user.name(), currentContext.getRemoteAddress(), time);
        // Update context:
        currentContext.setUser(validatedUser);
        Context.set(currentContext);

        // Register user (Send to cloud)
        IPCClient ipcClient = getDefaultIPCClient();
        if (ipcClient != null) {
            String jsonDataPacket = new Gson().toJson(validatedUser);
            ipcClient.sendData("REGISTER_USER$" + jsonDataPacket, false);
        }
    }
}
