package dev.aikido.agent_api.helpers.extraction;

import dev.aikido.agent_api.helpers.patterns.LooksLikeJWT;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static dev.aikido.agent_api.helpers.patterns.PrimitiveType.isPrimitiveType;

public final class StringExtractor {
    private StringExtractor() {}
    public static Map<String, String> extractStringsFromObject(Object obj) {
        return extractStringsRecursive(obj, new ArrayList<>());
    }
    private static Map<String, String> extractStringsRecursive(Object obj, ArrayList<PathBuilder.PathPart> pathToPayload) {
        Map<String, String> result = new HashMap<>();
        if (obj == null) {
            return Map.of();
        }
        if (obj instanceof String) {
            result.put((String) obj, PathBuilder.buildPathToPayload(pathToPayload));

            // Extract JWT Tokens :
            LooksLikeJWT.Result jwtResult = LooksLikeJWT.tryDecodeAsJwt((String) obj);
            if (jwtResult.success()) {
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                newPathToPayload.add(new PathBuilder.PathPart("jwt"));
                Map<String, String> resultsFromJWT = extractStringsRecursive(jwtResult.payload(), newPathToPayload);
                for (Map.Entry<String, String> entry : resultsFromJWT.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (key.equals("iss") || value.endsWith("<jwt>.iss")) {
                        // Do not add the issuer of the JWT as a string because it can contain a
                        // domain / url and produce false positives
                        continue;
                    }
                    result.put(key, value);
                }
            }
        }
        // We don't stringify arrays right now, it seems uncommon as an injection.
        else if (obj instanceof Collection<?>) {
            int index = 0;
            for (Object element : (Collection<?>) obj) {
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                newPathToPayload.add(new PathBuilder.PathPart("array", index));
                result.putAll(extractStringsRecursive(element, newPathToPayload));
                index++;
            }
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                newPathToPayload.add(new PathBuilder.PathPart("array", i));
                result.putAll(extractStringsRecursive(Array.get(obj, i), newPathToPayload));
            }
        } else if (obj instanceof Map<?, ?> map) {
            for (Object key : map.keySet()) {
                if (key instanceof String stringKey) {
                    result.put(stringKey, PathBuilder.buildPathToPayload(pathToPayload));
                }
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                if (!isPrimitiveType(key)) {
                    newPathToPayload.add(new PathBuilder.PathPart("object", "?")); // Use question mark for non-primitives
                } else {
                    newPathToPayload.add(new PathBuilder.PathPart("object", key.toString()));
                }
                result.putAll(extractStringsRecursive(map.get(key), newPathToPayload));
            }
        } else if (!isPrimitiveType(obj)) { // Stop algorithm if it's a primitive type.
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true); // Allow access to private fields
                    Object fieldValue = field.get(obj);
                    ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                    newPathToPayload.add(new PathBuilder.PathPart("object", field.getName()));
                    result.putAll(extractStringsRecursive(fieldValue, newPathToPayload));
                } catch (IllegalAccessException | RuntimeException ignored) {
                }
            }
        }
        return result;
    }
}
