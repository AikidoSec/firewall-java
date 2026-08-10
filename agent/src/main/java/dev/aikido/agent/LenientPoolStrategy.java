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

// Another agent (e.g. OpenTelemetry) can add synthetic supertypes with no .class resource to core types;
// the default pool then throws while resolving the hierarchy. Unresolvable types degrade to an empty interface.
public enum LenientPoolStrategy implements AgentBuilder.PoolStrategy {
    INSTANCE;

    @Override
    public TypePool typePool(ClassFileLocator classFileLocator, ClassLoader classLoader) {
        return new LenientPool(new TypePool.CacheProvider.Simple(), classFileLocator, TypePool.Default.ReaderMode.FAST);
    }

    @Override
    public TypePool typePool(ClassFileLocator classFileLocator, ClassLoader classLoader, String name) {
        return typePool(classFileLocator, classLoader);
    }

    private static final class LenientPool extends TypePool.Default {
        LenientPool(CacheProvider cacheProvider, ClassFileLocator classFileLocator, ReaderMode readerMode) {
            super(cacheProvider, classFileLocator, readerMode);
        }

        @Override
        protected Resolution doDescribe(String name) {
            Resolution resolution = super.doDescribe(name);
            return resolution.isResolved() ? resolution : new Resolution.Simple(new EmptyStubType(name));
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
