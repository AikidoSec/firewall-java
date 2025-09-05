package dev.aikido.agent_api.helpers;

import java.util.Collection;

public final class ArrayHelpers {
    private ArrayHelpers() {
    }

    public static String lastElement(String[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[array.length - 1];
    }

    public static boolean containsIgnoreCase(Collection<String> collection, String searchString) {
        for (String s : collection) {
            if (s.equalsIgnoreCase(searchString)) {
                return true;
            }
        }
        return false;
    }
}
