package dev.aikido.agent_api.context;

import java.util.concurrent.Callable;

public final class ContextPropagation {
    private ContextPropagation() {}

    public static Runnable wrap(Runnable task) {
        if (task == null || task instanceof ContextPropagatingRunnable) {
            return task;
        }

        ContextObject context = Context.get();
        if (context == null) {
            return task;
        }

        return new ContextPropagatingRunnable(task, context);
    }

    public static <T> Callable<T> wrap(Callable<T> task) {
        if (task == null || task instanceof ContextPropagatingCallable) {
            return task;
        }

        ContextObject context = Context.get();
        if (context == null) {
            return task;
        }

        return new ContextPropagatingCallable<>(task, context);
    }
}
