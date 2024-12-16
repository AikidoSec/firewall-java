package dev.aikido.agent_api.api_discovery;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record APISpec(
        Body body,
        DataSchemaItem query,
        List<Map<String, String>> auth
) implements Serializable {
    public record Body(DataSchemaItem schema, String type) implements Serializable {};
}
