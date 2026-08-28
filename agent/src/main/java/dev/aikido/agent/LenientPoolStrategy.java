package dev.aikido.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.RecordComponentDescription;
import net.bytebuddy.description.type.RecordComponentList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;

/*
 * OpenTelemetry adds helper interfaces to some classes it instruments. For example:
 *
 *   org.postgresql.jdbc.PgConnection
 *     └─ java.sql.Connection
 *          └─ io.opentelemetry.javaagent.bootstrap.field.VirtualFieldAccessor$...
 *
 * OpenTelemetry loads these helpers itself, so Zen cannot read their class files through the
 * application's class loader. Byte Buddy would remember the missing type and stop inspecting
 * the hierarchy before Zen can apply its instrumentation.
 *
 * For these known OpenTelemetry helpers, check for the class file before Byte Buddy caches the
 * miss. If it is unavailable, use an empty interface so Zen can keep inspecting the hierarchy.
 * Leave all other missing types unresolved.
 */
public enum LenientPoolStrategy implements AgentBuilder.PoolStrategy {
    INSTANCE;

    private static final String OTEL_VIRTUAL_FIELD_ACCESSOR_PREFIX =
            "io.opentelemetry.javaagent.bootstrap.field.VirtualFieldAccessor$";
    private static final String OTEL_VIRTUAL_FIELD_INSTALLED_MARKER =
            "io.opentelemetry.javaagent.bootstrap.field.VirtualFieldInstalledMarker";

    private static boolean isOtelVirtualField(String name) {
        return name.startsWith(OTEL_VIRTUAL_FIELD_ACCESSOR_PREFIX)
                || name.equals(OTEL_VIRTUAL_FIELD_INSTALLED_MARKER);
    }

    @Override
    public TypePool typePool(ClassFileLocator classFileLocator, ClassLoader classLoader) {
        return new TypePool.LazyFacade(new LenientPool(
                TypePool.CacheProvider.Simple.withObjectType(),
                classFileLocator,
                TypePool.Default.ReaderMode.FAST
        ));
    }

    @Override
    public TypePool typePool(ClassFileLocator classFileLocator, ClassLoader classLoader, String name) {
        return typePool(classFileLocator, classLoader);
    }

    private static final class LenientPool extends TypePool.Default.WithLazyResolution {
        LenientPool(CacheProvider cacheProvider, ClassFileLocator classFileLocator, ReaderMode readerMode) {
            super(cacheProvider, classFileLocator, readerMode);
        }

        @Override
        protected Resolution doDescribe(String name) {
            if (!isOtelVirtualField(name)) {
                return super.doDescribe(name);
            }

            try {
                if (!classFileLocator.locate(name).isResolved()) {
                    return new Resolution.Simple(new EmptyStubType(name));
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Error while reading class file", exception);
            }

            return super.doDescribe(name);
        }
    }

    private static final class EmptyStubType extends TypeDescription.Latent {
        EmptyStubType(String name) {
            super(name, Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE,
                    TypeDescription.Generic.OBJECT, Collections.<TypeDescription.Generic>emptyList());
        }

        @Override
        public MethodList<MethodDescription.InDefinedShape> getDeclaredMethods() {
            return new MethodList.Empty<MethodDescription.InDefinedShape>();
        }

        @Override
        public FieldList<FieldDescription.InDefinedShape> getDeclaredFields() {
            return new FieldList.Empty<FieldDescription.InDefinedShape>();
        }

        @Override
        public AnnotationList getDeclaredAnnotations() {
            return new AnnotationList.Empty();
        }

        @Override
        public RecordComponentList<RecordComponentDescription.InDefinedShape> getRecordComponents() {
            return new RecordComponentList.Empty<RecordComponentDescription.InDefinedShape>();
        }
    }
}
