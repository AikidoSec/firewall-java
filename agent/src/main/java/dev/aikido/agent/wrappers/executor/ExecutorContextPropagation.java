package dev.aikido.agent.wrappers.executor;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;

public final class ExecutorContextPropagation {
    private static Method wrapRunnableMethod;
    private static Method wrapCallableMethod;
    private static boolean disabled;

    private ExecutorContextPropagation() {}

    public static Runnable wrap(Runnable task) {
        if (task == null || disabled) {
            return task;
        }

        try {
            init();
            if (wrapRunnableMethod == null) {
                return task;
            }
            return (Runnable) wrapRunnableMethod.invoke(null, task);
        } catch (Throwable ignored) {
            disabled = true;
            return task;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Callable<T> wrap(Callable<T> task) {
        if (task == null || disabled) {
            return task;
        }

        try {
            init();
            if (wrapCallableMethod == null) {
                return task;
            }
            return (Callable<T>) wrapCallableMethod.invoke(null, task);
        } catch (Throwable ignored) {
            disabled = true;
            return task;
        }
    }

    private static synchronized void init() throws Exception {
        if (disabled || wrapRunnableMethod != null) {
            return;
        }

        String jarFilePath = System.getProperty("AIK_agent_api_jar");
        if (jarFilePath == null || jarFilePath.isBlank()) {
            disabled = true;
            return;
        }

        URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(jarFilePath) });
        Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.context.ContextPropagation");

        wrapRunnableMethod = clazz.getMethod("wrap", Runnable.class);
        wrapCallableMethod = clazz.getMethod("wrap", Callable.class);
    }
}
