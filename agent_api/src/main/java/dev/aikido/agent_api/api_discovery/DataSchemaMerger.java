package dev.aikido.agent_api.api_discovery;

import java.util.HashMap;
import java.util.Map;

public final class DataSchemaMerger {
    private DataSchemaMerger() {}

    public static DataSchemaItem mergeDataSchemas(DataSchemaItem first, DataSchemaItem second) {
        // Cannot merge different types
        if (!first.type().equals(second.type())) {
            // Prefer non-null type
            if (first.type().equals(DataSchemaType.EMPTY)) {
                return new DataSchemaItem(second);
            }
            return new DataSchemaItem(first); // Return the first schema
        }

        // Merge properties if they exist in both schemas
        if (first.properties() != null && second.properties() != null) {
            Map<String, DataSchemaItem> mergedProps = new HashMap<>(first.properties());

            for (Map.Entry<String, DataSchemaItem> secondProp : second.properties().entrySet()) {
                String key = secondProp.getKey();
                if (mergedProps.containsKey(key)) {
                    // Merge existing properties
                    mergedProps.put(key, mergeDataSchemas(mergedProps.get(key), secondProp.getValue()));
                } else {
                    // Add new property from the second schema
                    DataSchemaItem newSchema = new DataSchemaItem(
                            secondProp.getValue(),
                            /* optional: */ true
                    );
                    mergedProps.put(key,newSchema);
                }
            }
            for (Map.Entry<String, DataSchemaItem> firstProp : first.properties().entrySet()) {
                if (!second.properties().containsKey(firstProp.getKey())) {
                    // The key was removed in the second schema, mark as optional:
                    mergedProps.put(firstProp.getKey(), new DataSchemaItem(
                            firstProp.getValue(), /* optional: */ true));
                }
            }
            return new DataSchemaItem(first.type(), mergedProps);
        }

        // Merge items if they exist in both schemas
        if (first.items() != null && second.items() != null) {
            return new DataSchemaItem(
                /* type: */ first.type(),
                /* items: */ mergeDataSchemas(first.items(), second.items())
            );
        }

        return new DataSchemaItem(first);
    }
}
