package dev.aikido.agent;

import dev.aikido.agent_api.helpers.packages.RuntimePackageCollector;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

final class PackageObserver implements ClassFileTransformer {
    static void install(Instrumentation instrumentation) {
        RuntimePackageCollector.start();
        instrumentation.addTransformer(new PackageObserver(), false);
        for (Class<?> loadedClass : instrumentation.getAllLoadedClasses()) {
            observe(loadedClass);
        }
    }

    private static void observe(Class<?> loadedClass) {
        try {
            RuntimePackageCollector.observeClass(
                    loadedClass.getName(),
                    loadedClass.getProtectionDomain()
            );
        } catch (Throwable ignored) {
            // Package reporting must never interfere with agent startup.
        }
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer
    ) {
        RuntimePackageCollector.observeClass(className, protectionDomain);
        return null;
    }
}
