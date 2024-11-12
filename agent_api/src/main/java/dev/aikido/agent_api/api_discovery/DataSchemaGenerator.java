package dev.aikido.agent_api.api_discovery;

import dev.aikido.agent_api.helpers.extraction.PathBuilder;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static dev.aikido.agent_api.api_discovery.DataSchemaMerger.mergeDataSchemas;
import static dev.aikido.agent_api.helpers.patterns.PrimitiveType.isPrimitiveOrString;

public final class DataSchemaGenerator {
    private DataSchemaGenerator() {}
    // Maximum depth to traverse the data structure to get the schema for improved performance
    private static final int MAX_TRAVERSAL_DEPTH = 20;
    // Maximum amount of array members to merge into one
    private static final int MAX_ARRAY_DEPTH = 10;
    // Maximum number of properties per level
    private static final int MAX_PROPS = 100;

    public static DataSchemaItem getDataSchema(Object data) {
        return getDataSchema(data, 0);
    }

    private static DataSchemaItem getDataSchema(Object data, int depth) {
        if (data == null) {
            // Handle null as a special case
            return new DataSchemaItem(DataSchemaType.EMPTY);
        }
        if (isPrimitiveOrString(data)) {
            // Handle primitive types: (e.g. long, int, bool, strings, bytes, ...)
            return new DataSchemaItem(primitiveToType(data));
        }

        // Collection is a catch-all for lists, sets, ...
        if (data instanceof Collection<?> dataList) {
            return getDataSchema(dataList.toArray());
        }
        // Arrays are still another thing :
        if (data.getClass().isArray()) {
            DataSchemaItem items = null;
            for (int i = 0; i < Math.min(MAX_ARRAY_DEPTH, Array.getLength(data)); i++) {
                DataSchemaItem childDataSchemaItem = getDataSchema(Array.get(data, i));
                if (items == null) {
                    items = childDataSchemaItem;
                } else {
                    items = mergeDataSchemas(items, childDataSchemaItem); // Merge schemas
                }
            }
            return new DataSchemaItem(DataSchemaType.ARRAY, items);
        }

        // If the depth is less than the maximum depth, get the schema for each property
        Map<String, DataSchemaItem> props = new HashMap<>();
        if (depth <= MAX_TRAVERSAL_DEPTH) {
            if (data instanceof Map<?, ?> map) {
                for (Object key : map.keySet()) {
                    if (props.size() >= MAX_PROPS) {
                        // We cannot allow more properties than MAX_PROPS, breaking for loop.
                        break;
                    }
                    props.put((String) key, getDataSchema(map.get(key), depth + 1));
                }
            } else {
                Field[] fields = data.getClass().getDeclaredFields();
                for (Field field : fields) {
                    try {
                        field.setAccessible(true); // Allow access to private fields
                        props.put(field.getName(), getDataSchema(field.get(data), depth + 1));
                    } catch (IllegalAccessException | RuntimeException ignored) {
                    }
                }
            }
        }
        return new DataSchemaItem(DataSchemaType.OBJECT, props);
    }

    private static DataSchemaType primitiveToType(Object primitive) {
        if (primitive instanceof Number) {
            return DataSchemaType.NUMBER;
        } else if (primitive instanceof String) {
            return DataSchemaType.STRING;
        } else if (primitive instanceof Boolean) {
            return DataSchemaType.BOOL;
        }
        return DataSchemaType.OBJECT; // Unknown object
    }
}
