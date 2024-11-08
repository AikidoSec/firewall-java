package dev.aikido.agent_api.api_discovery;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DataSchemaItem(
        DataSchemaType type,
        DataSchemaItem items,
        Map<String, DataSchemaItem> properties,
        Boolean optional
) {
    public DataSchemaItem(DataSchemaType type) {
        // Set items and properties to null
        this(type, null, null, /* optional: */ false);
    }
    public DataSchemaItem(DataSchemaType type, DataSchemaItem items) {
        // Constructor for an array, sets properties to null
        this(type, items, null, /* optional: */ false);
    }
    public DataSchemaItem(DataSchemaType type, Map<String, DataSchemaItem> properties) {
        // Constructor for an object, sets items to null
        this(type, null, properties, /* optional: */ false);
    }
    public DataSchemaItem(DataSchemaItem second) {
        this(second.type(), second.items(), second.properties(), /* optional: */ false);
    }
    public DataSchemaItem(DataSchemaItem second, boolean optional) {
        this(second.type(), second.items(), second.properties(), optional);
    }
}
