package dev.aikido.agent_api.helpers;

public final class ArrayHelpers {
    private ArrayHelpers() {
    }

    public static String pop(String[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[array.length - 1];
    }
}
