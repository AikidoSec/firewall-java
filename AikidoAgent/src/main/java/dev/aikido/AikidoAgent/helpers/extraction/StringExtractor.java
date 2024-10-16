package dev.aikido.AikidoAgent.helpers.extraction;

import dev.aikido.AikidoAgent.helpers.patterns.LooksLikeJWT;

import java.lang.constant.Constable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static dev.aikido.AikidoAgent.helpers.patterns.PrimitiveType.isPrimitiveType;

public class StringExtractor {
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
            if (jwtResult.isSuccess()) {
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                newPathToPayload.add(new PathBuilder.PathPart("jwt"));
                Map<String, String> resultsFromJWT = extractStringsRecursive(jwtResult.getPayload(), newPathToPayload);
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
                if (!isPrimitiveType(key)) {
                    continue; // Key needs to be a primitive in order to create the path

                }
                if (key instanceof String) {
                    result.put((String) key, PathBuilder.buildPathToPayload(pathToPayload));
                }
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                newPathToPayload.add(new PathBuilder.PathPart("object", key.toString()));
                result.putAll(extractStringsRecursive(map.get(key), newPathToPayload));
            }
        } else if (isPrimitiveType(obj)) {
            // Do nothing, not a string, so don't check anymore.
        } else {
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
