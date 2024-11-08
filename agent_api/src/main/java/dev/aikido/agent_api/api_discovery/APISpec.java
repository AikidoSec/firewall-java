package dev.aikido.agent_api.api_discovery;

import java.util.List;
import java.util.Map;

public record APISpec(
        Body body,
        DataSchemaItem query,
        List<Map<String, String>> auth
) {
    public record Body(DataSchemaItem schema, String type) {};
}
