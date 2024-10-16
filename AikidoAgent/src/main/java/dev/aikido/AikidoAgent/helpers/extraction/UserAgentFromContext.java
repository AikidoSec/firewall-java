package dev.aikido.AikidoAgent.helpers.extraction;

import dev.aikido.AikidoAgent.context.ContextObject;

import java.util.Map;

public class UserAgentFromContext {
    public static String getUserAgent(ContextObject context) {
        for (Map.Entry<String, String> entry: context.getHeaders().entrySet()) {
            if (entry.getKey().equalsIgnoreCase("user-agent")) {
                return entry.getValue();
            }
        }
        return "Unknown User Agent";
    }
}
