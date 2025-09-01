package dev.aikido.agent_api;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

public class SetRateLimitGroup {
    private SetRateLimitGroup() {}
    private static final Logger logger = LogManager.getLogger(SetUser.class);

    public static void setRateLimitGroup(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            logger.info("Group ID or name cannot be empty.");
            return;
        }

        ContextObject currentContext = Context.get();
        if (currentContext == null) {
            logger.warn("setRateLimitGroup(...) was called without a context. Make sure to call setRateLimitGroup(...) within an HTTP request.");
            return;
        }
        if (currentContext.middlewareExecuted()) {
            logger.info(
                "setRateLimitGroup(...) must be called before the Zen middleware is executed."
            );
        }

        currentContext.setRateLimitGroup(groupId);
    }
}
