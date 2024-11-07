package dev.aikido.agent_api.api_discovery;

import java.util.List;
import java.util.Map;

public record APISpec(
        Object body,
        DataSchemaItem query,
        List<Map<String, String>> auth
) {}
