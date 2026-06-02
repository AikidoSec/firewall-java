package context;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.ContextPropagatingCallable;
import dev.aikido.agent_api.context.ContextPropagatingRunnable;
import dev.aikido.agent_api.context.ContextPropagation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class ContextPropagationTest {
    @AfterEach
    void tearDown() {
        Context.reset();
    }

    @Test
    void wrapRunnableReturnsNullForNullTask() {
        Assertions.assertNull(ContextPropagation.wrap((Runnable) null));
    }

    @Test
    void wrapCallableReturnsNullForNullTask() {
        Assertions.assertNull(ContextPropagation.wrap((Callable<Object>) null));
    }

    @Test
    void wrapRunnableReturnsOriginalTaskWhenNoContextIsSet() {
        Runnable task = () -> {};

        Runnable wrapped = ContextPropagation.wrap(task);

        Assertions.assertSame(task, wrapped);
    }

    @Test
    void wrapCallableReturnsOriginalTaskWhenNoContextIsSet() {
        Callable<String> task = () -> "ok";

        Callable<String> wrapped = ContextPropagation.wrap(task);

        Assertions.assertSame(task, wrapped);
    }

    @Test
    void wrapRunnableReturnsSameTaskWhenAlreadyWrapped() {
        ContextObject contextObject = new ContextObject();
        Runnable task = new ContextPropagatingRunnable(() -> {}, contextObject);

        Runnable wrapped = ContextPropagation.wrap(task);

        Assertions.assertSame(task, wrapped);
    }

    @Test
    void wrapCallableReturnsSameTaskWhenAlreadyWrapped() {
        ContextObject contextObject = new ContextObject();
        Callable<String> task = new ContextPropagatingCallable<>(() -> "ok", contextObject);

        Callable<String> wrapped = ContextPropagation.wrap(task);

        Assertions.assertSame(task, wrapped);
    }

    @Test
    void wrapRunnableCapturesCurrentContext() {
        ContextObject requestContext = new ContextObject();
        Context.set(requestContext);

        AtomicReference<ContextObject> contextDuringRun = new AtomicReference<>();
        Runnable wrapped = ContextPropagation.wrap(() -> contextDuringRun.set(Context.get()));

        Context.reset();
        wrapped.run();

        Assertions.assertSame(requestContext, contextDuringRun.get());
        Assertions.assertNull(Context.get(), "Expected worker context to be cleared after task execution");
    }

    @Test
    void wrapCallableCapturesCurrentContext() throws Exception {
        ContextObject requestContext = new ContextObject();
        Context.set(requestContext);

        Callable<ContextObject> wrapped = ContextPropagation.wrap(Context::get);

        Context.reset();
        ContextObject contextDuringCall = wrapped.call();

        Assertions.assertSame(requestContext, contextDuringCall);
        Assertions.assertNull(Context.get(), "Expected worker context to be cleared after task execution");
    }

    @Test
    void contextPropagatingRunnableRestoresPreviousWorkerContext() {
        ContextObject capturedContext = new ContextObject();
        ContextObject previousWorkerContext = new ContextObject();

        ContextPropagatingRunnable task = new ContextPropagatingRunnable(
            () -> Assertions.assertSame(capturedContext, Context.get()),
            capturedContext
        );

        Context.set(previousWorkerContext);
        task.run();

        Assertions.assertSame(previousWorkerContext, Context.get());
    }

    @Test
    void contextPropagatingCallableRestoresPreviousWorkerContext() throws Exception {
        ContextObject capturedContext = new ContextObject();
        ContextObject previousWorkerContext = new ContextObject();

        ContextPropagatingCallable<ContextObject> task = new ContextPropagatingCallable<>(
            Context::get,
            capturedContext
        );

        Context.set(previousWorkerContext);
        ContextObject contextDuringCall = task.call();

        Assertions.assertSame(capturedContext, contextDuringCall);
        Assertions.assertSame(previousWorkerContext, Context.get());
    }

    @Test
    void contextPropagatingRunnableClearsContextWhenWorkerHadNoPreviousContext() {
        ContextObject capturedContext = new ContextObject();

        ContextPropagatingRunnable task = new ContextPropagatingRunnable(
            () -> Assertions.assertSame(capturedContext, Context.get()),
            capturedContext
        );

        Context.reset();
        task.run();

        Assertions.assertNull(Context.get());
    }

    @Test
    void contextPropagatingCallableClearsContextWhenWorkerHadNoPreviousContext() throws Exception {
        ContextObject capturedContext = new ContextObject();

        ContextPropagatingCallable<ContextObject> task = new ContextPropagatingCallable<>(
            Context::get,
            capturedContext
        );

        Context.reset();
        ContextObject contextDuringCall = task.call();

        Assertions.assertSame(capturedContext, contextDuringCall);
        Assertions.assertNull(Context.get());
    }

    @Test
    void contextPropagatingRunnableRestoresPreviousWorkerContextAfterException() {
        ContextObject capturedContext = new ContextObject();
        ContextObject previousWorkerContext = new ContextObject();

        ContextPropagatingRunnable task = new ContextPropagatingRunnable(
            () -> {
                throw new IllegalStateException("boom");
            },
            capturedContext
        );

        Context.set(previousWorkerContext);

        Assertions.assertThrows(IllegalStateException.class, task::run);
        Assertions.assertSame(previousWorkerContext, Context.get());
    }

    @Test
    void contextPropagatingCallableRestoresPreviousWorkerContextAfterException() {
        ContextObject capturedContext = new ContextObject();
        ContextObject previousWorkerContext = new ContextObject();

        ContextPropagatingCallable<String> task = new ContextPropagatingCallable<>(
            () -> {
                throw new IllegalStateException("boom");
            },
            capturedContext
        );

        Context.set(previousWorkerContext);

        Assertions.assertThrows(IllegalStateException.class, task::call);
        Assertions.assertSame(previousWorkerContext, Context.get());
    }

    @Test
    void wrappedRunnableRunsDelegate() {
        ContextObject requestContext = new ContextObject();
        Context.set(requestContext);

        AtomicBoolean delegateCalled = new AtomicBoolean(false);
        Runnable wrapped = ContextPropagation.wrap(() -> delegateCalled.set(true));

        Context.reset();
        wrapped.run();

        Assertions.assertTrue(delegateCalled.get());
    }

    @Test
    void wrappedCallableReturnsDelegateResult() throws Exception {
        ContextObject requestContext = new ContextObject();
        Context.set(requestContext);

        Callable<String> wrapped = ContextPropagation.wrap(() -> "ok");

        Context.reset();

        Assertions.assertEquals("ok", wrapped.call());
    }
}
