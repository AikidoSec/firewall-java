package dev.aikido.agent_api.api_discovery;

import java.lang.reflect.*;
import java.util.*;

import static dev.aikido.agent_api.api_discovery.DataSchemaMerger.mergeDataSchemas;
import static dev.aikido.agent_api.helpers.patterns.PrimitiveType.isPrimitiveOrString;

public class DataSchemaGenerator {
    // Maximum depth to traverse the data structure to get the schema for improved performance
    private static final int MAX_TRAVERSAL_DEPTH = 20;
    // Maximum amount of array members to merge into one
    private static final int MAX_ARRAY_DEPTH = 10;
    // Maximum number of properties per level
    private static final int MAX_PROPS = 100;

    public static DataSchemaItem getDataSchema(Object data) {
        // We use an IdentityHashMap to skip the hashCode() and equals() checks
        // these checks can take longer than a simple identity check, and in rare cases
        // be themselves recursive.
        Set<Object> scanned = Collections.newSetFromMap(new IdentityHashMap<>());
        return new DataSchemaGenerator().getDataSchema(data, 0, scanned);
    }

    private DataSchemaItem getDataSchema(Object data, int depth, Set<Object> scanned) {
        if (depth > MAX_TRAVERSAL_DEPTH) {
            // avoid expensive recursion loops
            return new DataSchemaItem(DataSchemaType.EMPTY);
        }
        depth += 1;

        if (data == null || scanned.contains(data)) {
            // Handle null as a special case
            return new DataSchemaItem(DataSchemaType.EMPTY);
        }

        if (isPrimitiveOrString(data)) {
            // Handle primitive types: (e.g. long, int, bool, strings, bytes, ...)
            return new DataSchemaItem(primitiveToType(data));
        }

        // Don't add primitive types to the scanned list (which avoids slow & heavy recursions)
        scanned.add(data);

        // Collection is a catch-all for lists, sets, ...
        if (data instanceof Collection<?> dataList) {
            return getDataSchema(dataList.toArray(), depth, scanned);
        }
        // Arrays are still another thing :
        if (data.getClass().isArray()) {
            DataSchemaItem items = null;
            for (int i = 0; i < Math.min(MAX_ARRAY_DEPTH, Array.getLength(data)); i++) {
                DataSchemaItem childDataSchemaItem = getDataSchema(Array.get(data, i), depth, scanned);
                if (items == null) {
                    items = childDataSchemaItem;
                } else {
                    items = mergeDataSchemas(items, childDataSchemaItem); // Merge schemas
                }
            }
            return new DataSchemaItem(DataSchemaType.ARRAY, items);
        }
        if(data.getClass().isEnum()) {
            // Handle enums differently as to avoid recursion :
            return new DataSchemaItem(DataSchemaType.ENUM);
        }

        // If the depth is less than the maximum depth, get the schema for each property
        Map<String, DataSchemaItem> props = new HashMap<>();
        if (data instanceof Map<?, ?> map) {
            for (Object key : map.keySet()) {
                if (props.size() >= MAX_PROPS) {
                    // We cannot allow more properties than MAX_PROPS, breaking for loop.
                    break;
                }
                props.put((String) key, getDataSchema(map.get(key), depth, scanned));
            }
        } else if (data.getClass().toString().startsWith("class org.codehaus.groovy")) {
            // pass through, we do not want to check org.codehaus.groovy
        } else {
            Field[] fields = data.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    if (Modifier.isTransient(field.getModifiers())) {
                        continue; // Do not scan transient fields.
                    }
                    field.setAccessible(true); // Allow access to private fields
                    if (props.size() >= MAX_PROPS) {
                        // We cannot allow more properties than MAX_PROPS, breaking for loop.
                        break;
                    }
                    props.put(field.getName(), getDataSchema(field.get(data), depth, scanned));
                } catch (IllegalAccessException | RuntimeException ignored) {
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
