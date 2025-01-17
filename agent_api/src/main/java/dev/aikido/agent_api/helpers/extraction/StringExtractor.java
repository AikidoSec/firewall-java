package dev.aikido.agent_api.helpers.extraction;

import dev.aikido.agent_api.helpers.patterns.LooksLikeJWT;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static dev.aikido.agent_api.helpers.extraction.PathBuilder.buildPathToPayload;
import static dev.aikido.agent_api.helpers.patterns.PrimitiveType.isPrimitiveType;

public class StringExtractor {
    // Ensures that we don't get recursion :
    Set<Object> scanned = new HashSet<>();
    public static Map<String, String> extractStringsFromObject(Object obj) {
        return new StringExtractor().extractStringsRecursive(obj, new ArrayList<>());
    }
    private Map<String, String> extractStringsRecursive(Object target, ArrayList<PathBuilder.PathPart> pathToPayload) {
        HashMap<String, String> result = new HashMap<>();
        if (target == null || scanned.contains(target)) {
            return Map.of(); // Do not rescan objects, because this might lead to recursion.
        }
        scanned.add(target);

        if (target instanceof String targetString) {
            result.putAll(extractStringsFromString(targetString, pathToPayload));
        } else if (target instanceof Collection<?> || target.getClass().isArray()) {
            result.putAll(extractStringsFromArray(target, pathToPayload));
        } else if (target instanceof Map<?, ?> targetMap) {
            result.putAll(extractStringsFromMap(targetMap, pathToPayload));
        }
        //else if (!isPrimitiveType(target)) { // Stop algorithm if it's a primitive type.
        //    result.putAll(extractStringsFromStructure(target, pathToPayload));
        //}
        return result;
    }

    private Map<String, String> extractStringsFromString(String target, ArrayList<PathBuilder.PathPart> pathToPayload) {
        HashMap<String, String> result = new HashMap<>();
        result.put(target, buildPathToPayload(pathToPayload));

        // Extract JWT Tokens :
        LooksLikeJWT.Result jwtResult = LooksLikeJWT.tryDecodeAsJwt(target);
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
        return result;
    }

    private Map<String, String> extractStringsFromArray(Object target, ArrayList<PathBuilder.PathPart> pathToPayload) {
        HashMap<String, String> result = new HashMap<>();
        if (target instanceof Collection<?> targetCollection) {
            int index = 0;
            for (Object element : (Collection<?>) targetCollection) {
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                newPathToPayload.add(new PathBuilder.PathPart("array", index));
                result.putAll(extractStringsRecursive(element, newPathToPayload));
                index++;
            }
        } else if (target instanceof Object[] targetArray) {
            for (int i = 0; i < targetArray.length; i++) {
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                newPathToPayload.add(new PathBuilder.PathPart("array", i));
                result.putAll(extractStringsRecursive(targetArray[i], newPathToPayload));
            }
        }
        return result;
    }

    private Map<String, String> extractStringsFromMap(Map<?, ?> target, ArrayList<PathBuilder.PathPart> pathToPayload) {
        HashMap<String, String> result = new HashMap<>();
        for (Object key : target.keySet()) {
            if (key instanceof String stringKey) {
                result.put(stringKey, buildPathToPayload(pathToPayload));
            }
            ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
            if (!isPrimitiveType(key)) {
                newPathToPayload.add(new PathBuilder.PathPart("object", "?")); // Use question mark for non-primitives
            } else {
                newPathToPayload.add(new PathBuilder.PathPart("object", key.toString()));
            }
            result.putAll(extractStringsRecursive(target.get(key), newPathToPayload));
        }
        return result;
    }

    private Map<String, String> extractStringsFromStructure(Object target, ArrayList<PathBuilder.PathPart> pathToPayload) {
        HashMap<String, String> result = new HashMap<>();
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue; // Do not scan transient fields.
                }
                field.setAccessible(true); // Allow access to private fields
                Object fieldValue = field.get(target);
                ArrayList<PathBuilder.PathPart> newPathToPayload = new ArrayList<>(pathToPayload);
                newPathToPayload.add(new PathBuilder.PathPart("object", field.getName()));
                result.putAll(extractStringsRecursive(fieldValue, newPathToPayload));
            } catch (IllegalAccessException | RuntimeException e) {
                // pass-through
            }
        }
        return result;
    }
}
