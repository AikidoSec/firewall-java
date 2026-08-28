package dev.aikido.agent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.StubMethod;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.pool.TypePool;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.declaresMethod;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LenientPoolStrategyTest {
    private static final String OTEL_VIRTUAL_FIELD_ACCESSOR =
            "io.opentelemetry.javaagent.bootstrap.field.VirtualFieldAccessor$java$lang$Runnable$context";
    private static final String OTEL_VIRTUAL_FIELD_INSTALLED_MARKER =
            "io.opentelemetry.javaagent.bootstrap.field.VirtualFieldInstalledMarker";
    private static final String CONTROLLER_BASE = "com.acme.ControllerBase";
    private static final String CONTROLLER = "com.acme.Controller";

    private TypePool pool() {
        ClassLoader classLoader = getClass().getClassLoader();
        return LenientPoolStrategy.INSTANCE.typePool(ClassFileLocator.ForClassLoader.of(classLoader), classLoader);
    }

    @Test
    void virtualFieldInterfacesDegradeToEmptyInterface() {
        for (String virtualFieldInterface : List.of(
                OTEL_VIRTUAL_FIELD_ACCESSOR,
                OTEL_VIRTUAL_FIELD_INSTALLED_MARKER
        )) {
            TypeDescription type = pool().describe(virtualFieldInterface).resolve();

            assertEquals(virtualFieldInterface, type.getName());
            assertTrue(type.isInterface());
            assertTrue(type.getDeclaredMethods().isEmpty());
            assertTrue(type.getDeclaredFields().isEmpty());
            assertTrue(type.getInterfaces().isEmpty());
            assertEquals(Object.class.getName(), type.getSuperClass().asErasure().getName());
        }
    }

    @Test
    void nonOtelMissingTypeRemainsUnresolved() {
        assertFalse(pool().describe("com.acme.MissingDependency").isResolved());
    }

    @Test
    void matchesHierarchyContainingVirtualFieldAccessor() {
        TypePool pool = LenientPoolStrategy.INSTANCE.typePool(
                new ClassFileLocator.Simple(Map.of(
                        CONTROLLER_BASE, controllerBaseBytes(),
                        CONTROLLER, controllerBytes()
                )),
                null
        );

        TypeDescription controller = pool.describe(CONTROLLER).resolve();

        assertTrue(hasSuperType(declaresMethod(named("handle"))).matches(controller));
        assertFalse(hasSuperType(declaresMethod(named("missing"))).matches(controller));
    }

    private static byte[] controllerBaseBytes() {
        return new ByteBuddy()
                .subclass(Object.class)
                .name(CONTROLLER_BASE)
                .defineMethod("handle", void.class, Modifier.PUBLIC)
                .intercept(StubMethod.INSTANCE)
                .make()
                .getBytes();
    }

    private static byte[] controllerBytes() {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC,
                CONTROLLER.replace('.', '/'),
                null,
                CONTROLLER_BASE.replace('.', '/'),
                new String[] {OTEL_VIRTUAL_FIELD_ACCESSOR.replace('.', '/')}
        );
        MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                CONTROLLER_BASE.replace('.', '/'),
                "<init>",
                "()V",
                false
        );
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }
}
