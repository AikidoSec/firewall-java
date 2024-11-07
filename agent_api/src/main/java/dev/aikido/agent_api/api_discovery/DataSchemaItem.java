package dev.aikido.agent_api.api_discovery;

import java.util.List;
import java.util.Map;

public record DataSchemaItem(
        String type,
        DataSchemaItem items,
        Map<String, DataSchemaItem> properties
) {
    public DataSchemaItem(DataSchemaType type) {
        // Set items and properties to null
        this(type.toString(), null, null);
    }
    public DataSchemaItem(DataSchemaType type, DataSchemaItem items) {
        // Constructor for an array, sets properties to null
        this(type.toString(), items, null);
    }
    public DataSchemaItem(DataSchemaType type, Map<String, DataSchemaItem> properties) {
        // Constructor for an object, sets items to null
        this(type.toString(), null, properties);
    }
}
