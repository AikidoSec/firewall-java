package dev.aikido.agent_api.context;

import java.util.concurrent.Callable;

public final class ContextPropagatingCallable<T> implements Callable<T> {
    private final Callable<T> delegate;
    private final ContextObject context;

    public ContextPropagatingCallable(Callable<T> delegate, ContextObject context) {
        this.delegate = delegate;
        this.context = context;
    }

    @Override
    public T call() throws Exception {
        ContextObject previousContext = Context.get();

        try {
            Context.set(context);
            return delegate.call();
        } finally {
            if (previousContext != null) {
                Context.set(previousContext);
            } else {
                Context.reset();
            }
        }
    }
}
