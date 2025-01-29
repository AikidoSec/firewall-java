package dev.aikido.agent_api.helpers.extraction;

import dev.aikido.agent_api.context.ContextObject;
import java.util.Map;

public final class UserAgentFromContext {
    private UserAgentFromContext() {}

    public static String getUserAgent(ContextObject context) {
        for (Map.Entry<String, String> entry : context.getHeaders().entrySet()) {
            if (entry.getKey().equalsIgnoreCase("user-agent")) {
                return entry.getValue();
            }
        }
        return "Unknown User Agent";
    }
}
