package dev.aikido.agent_api.helpers.patterns;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.*;

public final class PrimitiveType {
    private PrimitiveType() {}
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
        // Also mark the atomic ones as primitive :
        WRAPPER_TYPE_MAP.put(AtomicInteger.class, AtomicInteger.class);
        WRAPPER_TYPE_MAP.put(AtomicBoolean.class, AtomicBoolean.class);
        WRAPPER_TYPE_MAP.put(AtomicLong.class, AtomicLong.class);
        WRAPPER_TYPE_MAP.put(AtomicIntegerArray.class, AtomicIntegerArray.class);
        WRAPPER_TYPE_MAP.put(AtomicLongArray.class, AtomicLongArray.class);
    }
    public static boolean isPrimitiveType(Object source) {
        return WRAPPER_TYPE_MAP.containsKey(source.getClass());
    }
    public static boolean isPrimitiveOrString(Object source) {
        if (WRAPPER_TYPE_MAP.containsKey(source.getClass())) {
            return true;
        }
        if (source instanceof Number) {
            // Add special case for numbers, since it's hard to put all different number types in this map.
            return true;
        }
        return source instanceof String;
    }
}
