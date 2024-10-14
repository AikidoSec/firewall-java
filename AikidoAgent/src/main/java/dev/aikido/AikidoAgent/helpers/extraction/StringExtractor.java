package dev.aikido.AikidoAgent.helpers.extraction;

import java.lang.constant.Constable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class StringExtractor {
    public static Map<String, String> extractStringsFromObject(Object obj) {
        HashMap<String, String> result = new HashMap<>();
        extractStringsRecursive(obj, result, ".");
        return result;
    }
    private static void extractStringsRecursive(Object obj, Map<String, String> result, String fieldName) {
        if (obj == null) {
            return;
        }

        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object arrayElement = Array.get(obj, i);
                extractStringsRecursive(arrayElement, result, "array");
            }
            return;
        }

        // If the object is a string, add it to the result
        switch (obj) {
            case String s -> {
                result.put(fieldName, s);
                return;
            }
            case Collection<?> objects -> {
                int index = 0;
                for (Object element : objects) {
                    extractStringsRecursive(element, result, fieldName + "." + index);
                    index++;
                }
                return;
            }

            // Otherwise, inspect the fields of the object
            case Constable constable -> {
                return;
            }
            default -> {
            }
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true); // Allow access to private fields
                Object fieldValue = field.get(obj);
                extractStringsRecursive(fieldValue, result, field.getName());
            } catch (IllegalAccessException | RuntimeException ignored) {
            }
        }
    }
}
