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

import java.lang.reflect.Modifier;
import java.util.Collections;

/*
 * OTel adds generated VirtualFieldAccessor interfaces to types it instruments. For example:
 *
 *   org.postgresql.jdbc.PgConnection
 *     └─ java.sql.Connection
 *          └─ io.opentelemetry.javaagent.bootstrap.field.VirtualFieldAccessor$...
 *
 * OTel provides the generated interface bytecode itself, so it has no .class resource for Zen's
 * ClassFileLocator. Byte Buddy must resolve the hierarchy eagerly to replace this specific missing
 * interface with an empty stub before Zen's matchers traverse it. Other missing types stay unresolved.
 */
public enum LenientPoolStrategy implements AgentBuilder.PoolStrategy {
    INSTANCE;

    private static final String OTEL_VIRTUAL_FIELD_ACCESSOR_PREFIX =
            "io.opentelemetry.javaagent.bootstrap.field.VirtualFieldAccessor$";

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
            if (!name.startsWith(OTEL_VIRTUAL_FIELD_ACCESSOR_PREFIX)) {
                return super.doDescribe(name);
            }

            Resolution resolution = super.doDescribe(name);
            return resolution.isResolved()
                    ? resolution
                    : new Resolution.Simple(new EmptyStubType(name));
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
