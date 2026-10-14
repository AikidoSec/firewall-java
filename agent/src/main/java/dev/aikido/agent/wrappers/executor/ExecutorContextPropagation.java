package dev.aikido.agent.wrappers.executor;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;

// Bridges the executor advice (woven into java.util.concurrent classes) to ContextPropagation in
// agent_api, which lives on a different classloader, and caches the reflected methods so the lookup
// happens once. A missing AIK_agent_api_jar is treated as "not ready yet" (early startup) and
// retried on the next call, rather than disabling propagation for the whole JVM; only a genuine
// load failure once the path is set disables it.
public final class ExecutorContextPropagation {
    private static volatile Method wrapRunnableMethod;
    private static volatile Method wrapCallableMethod;
    private static volatile boolean disabled;

    private ExecutorContextPropagation() {}

    public static Runnable wrap(Runnable task) {
        if (task == null) {
            return task;
        }
        Method wrap = wrapRunnableMethod;
        if (wrap == null) {
            init();
            wrap = wrapRunnableMethod;
        }
        if (wrap == null) {
            return task;
        }
        try {
            return (Runnable) wrap.invoke(null, task);
        } catch (Throwable ignored) {
            return task;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Callable<T> wrap(Callable<T> task) {
        if (task == null) {
            return task;
        }
        Method wrap = wrapCallableMethod;
        if (wrap == null) {
            init();
            wrap = wrapCallableMethod;
        }
        if (wrap == null) {
            return task;
        }
        try {
            return (Callable<T>) wrap.invoke(null, task);
        } catch (Throwable ignored) {
            return task;
        }
    }

    private static synchronized void init() {
        if (disabled || wrapRunnableMethod != null) {
            return;
        }
        String jarFilePath = System.getProperty("AIK_agent_api_jar");
        if (jarFilePath == null || jarFilePath.isBlank()) {
            return; // not set yet during early startup - retry on a later call
        }
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(jarFilePath) });
            Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.context.ContextPropagation");
            wrapCallableMethod = clazz.getMethod("wrap", Callable.class);
            wrapRunnableMethod = clazz.getMethod("wrap", Runnable.class);
        } catch (Throwable ignored) {
            disabled = true;
        }
    }
}
