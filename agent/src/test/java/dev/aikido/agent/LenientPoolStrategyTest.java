package dev.aikido.agent;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LenientPoolStrategyTest {
    private TypePool pool() {
        ClassLoader classLoader = getClass().getClassLoader();
        return LenientPoolStrategy.INSTANCE.typePool(ClassFileLocator.ForClassLoader.of(classLoader), classLoader);
    }

    @Test
    void resolvableTypeIsDescribedNormally() {
        TypeDescription type = pool().describe("java.util.ArrayList").resolve();

        assertEquals("java.util.ArrayList", type.getName());
        assertFalse(type.isInterface());
        assertFalse(type.getDeclaredMethods().isEmpty());
    }

    @Test
    void unresolvableTypeDegradesToEmptyInterface() {
        TypeDescription type = pool().describe("com.acme.Injected$VirtualField$Absent").resolve();

        assertEquals("com.acme.Injected$VirtualField$Absent", type.getName());
        assertTrue(type.isInterface());
        assertTrue(type.getDeclaredMethods().isEmpty());
        assertTrue(type.getDeclaredFields().isEmpty());
        assertTrue(type.getInterfaces().isEmpty());
        assertEquals(Object.class.getName(), type.getSuperClass().asErasure().getName());
    }
}
