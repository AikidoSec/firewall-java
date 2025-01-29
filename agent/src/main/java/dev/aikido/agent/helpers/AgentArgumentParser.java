package dev.aikido.agent.helpers;

import java.util.HashMap;
import java.util.Map;

public final class AgentArgumentParser {
    private AgentArgumentParser() {}

    public static Map<String, String> parseAgentArgs(String agentArgs) {
        Map<String, String> map = new HashMap<>();
        if (agentArgs == null) {
            return map;
        }

        String[] args = agentArgs.split(",");
        for (String arg : args) {
            String[] keyValue = arg.split("=");
            if (keyValue.length == 2) {
                // Put the key-value pair into the map :
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return map;
    }
}
