package dev.aikido.AikidoAgent.helpers.patterns;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveType {
    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;
    static {
        WRAPPER_TYPE_MAP = new HashMap<Class<?>, Class<?>>(16);
        WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        WRAPPER_TYPE_MAP.put(Character.class, char.class);
        WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_TYPE_MAP.put(Double.class, double.class);
        WRAPPER_TYPE_MAP.put(Float.class, float.class);
        WRAPPER_TYPE_MAP.put(Long.class, long.class);
        WRAPPER_TYPE_MAP.put(Short.class, short.class);
        WRAPPER_TYPE_MAP.put(String.class, String.class); // Yes also see Strings as primitive.
    }
    public static boolean isPrimitiveType(Object source) {
        System.out.println(source.getClass());
        return WRAPPER_TYPE_MAP.containsKey(source.getClass());
    }
}
