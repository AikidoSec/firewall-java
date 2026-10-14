package dev.aikido.agent_api.context;

public final class ContextPropagatingRunnable implements Runnable {
    private final Runnable delegate;
    private final ContextObject context;

    public ContextPropagatingRunnable(Runnable delegate, ContextObject context) {
        this.delegate = delegate;
        this.context = context;
    }

    @Override
    public void run() {
        ContextObject previous = Context.get();
        try {
            Context.set(context);
            delegate.run();
        } finally {
            Context.restore(previous);
        }
    }
}
