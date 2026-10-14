package context;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.ContextPropagatingCallable;
import dev.aikido.agent_api.context.ContextPropagatingRunnable;
import dev.aikido.agent_api.context.ContextPropagation;
import dev.aikido.agent_api.storage.RedirectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
        requestContext.setRateLimitGroup("req");
        Context.set(requestContext);

        AtomicReference<ContextObject> contextDuringRun = new AtomicReference<>();
        Runnable wrapped = ContextPropagation.wrap(() -> contextDuringRun.set(Context.get()));

        Context.reset();
        wrapped.run();

        Assertions.assertEquals("req", contextDuringRun.get().getRateLimitGroup());
        Assertions.assertNull(Context.get(), "Expected worker context to be cleared after task execution");
    }

    @Test
    void wrapCallableCapturesCurrentContext() throws Exception {
        ContextObject requestContext = new ContextObject();
        requestContext.setRateLimitGroup("req");
        Context.set(requestContext);

        Callable<ContextObject> wrapped = ContextPropagation.wrap(Context::get);

        Context.reset();
        ContextObject contextDuringCall = wrapped.call();

        Assertions.assertEquals("req", contextDuringCall.getRateLimitGroup());
        Assertions.assertNull(Context.get(), "Expected worker context to be cleared after task execution");
    }

    @Test
    void wrapPropagatesAnIsolatedSnapshotNotTheLiveContext() {
        ContextObject requestContext = new ContextObject();
        Context.set(requestContext);

        AtomicReference<ContextObject> contextDuringRun = new AtomicReference<>();
        Runnable wrapped = ContextPropagation.wrap(() -> contextDuringRun.set(Context.get()));

        Context.reset();
        wrapped.run();

        ContextObject propagated = contextDuringRun.get();
        Assertions.assertNotSame(requestContext, propagated);
        Assertions.assertNotSame(requestContext.getCache(), propagated.getCache());
    }

    @Test
    void snapshotDeepCopiesRedirectChainNodes() throws Exception {
        RedirectableContext requestContext = new RedirectableContext();
        RedirectNode starter = new RedirectNode(new URL("http://origin"));
        new RedirectNode(starter, new URL("http://dest"));
        requestContext.addRedirectNode(starter);

        ContextObject copy = requestContext.copyForPropagation();

        RedirectNode originalStarter = requestContext.getRedirectStartNodes().get(0);
        RedirectNode copiedStarter = copy.getRedirectStartNodes().get(0);

        Assertions.assertNotSame(originalStarter, copiedStarter);
        Assertions.assertNotSame(originalStarter.getChild(), copiedStarter.getChild());
        Assertions.assertEquals("http://dest", copiedStarter.getChild().getUrl().toString());
    }

    @Test
    void snapshotDeepCopiesCacheInnerMaps() {
        ContextObject requestContext = new ContextObject();
        Map<String, String> inner = new HashMap<>();
        inner.put("k", "v");
        requestContext.getCache().put("body", inner);

        ContextObject copy = requestContext.copyForPropagation();

        Assertions.assertNotSame(requestContext.getCache().get("body"), copy.getCache().get("body"));
        Assertions.assertEquals("v", copy.getCache().get("body").get("k"));
    }

    @Test
    void snapshotCacheMutationDoesNotLeakToOriginal() {
        ContextObject requestContext = new ContextObject();
        Map<String, String> inner = new HashMap<>();
        inner.put("k", "v");
        requestContext.getCache().put("body", inner);

        ContextObject copy = requestContext.copyForPropagation();
        copy.getCache().get("body").put("injected", "x");
        copy.getCache().put("query", new HashMap<>());

        Assertions.assertFalse(requestContext.getCache().get("body").containsKey("injected"));
        Assertions.assertFalse(requestContext.getCache().containsKey("query"));
    }

    @Test
    void snapshotRedirectChainMutationDoesNotLeakToOriginal() throws Exception {
        RedirectableContext requestContext = new RedirectableContext();
        RedirectNode starter = new RedirectNode(new URL("http://origin"));
        new RedirectNode(starter, new URL("http://dest"));
        requestContext.addRedirectNode(starter);

        ContextObject copy = requestContext.copyForPropagation();
        RedirectNode copiedDest = copy.getRedirectStartNodes().get(0).getChild();
        new RedirectNode(copiedDest, new URL("http://extra"));

        RedirectNode originalDest = requestContext.getRedirectStartNodes().get(0).getChild();
        Assertions.assertNull(originalDest.getChild());
    }

    @Test
    void copyChainPreservesMultiNodeChain() throws Exception {
        RedirectNode a = new RedirectNode(new URL("http://a"));
        RedirectNode b = new RedirectNode(a, new URL("http://b"));
        RedirectNode c = new RedirectNode(b, new URL("http://c"));

        RedirectNode copy = a.copyChain();

        Assertions.assertEquals("http://a", copy.getUrl().toString());
        Assertions.assertEquals("http://b", copy.getChild().getUrl().toString());
        Assertions.assertEquals("http://c", copy.getChild().getChild().getUrl().toString());
        Assertions.assertNotSame(b, copy.getChild());
        Assertions.assertNotSame(c, copy.getChild().getChild());
    }

    @Test
    void snapshotHeaderListMutationDoesNotLeakToOriginal() {
        HeaderfulContext requestContext = new HeaderfulContext();
        ArrayList<String> values = new ArrayList<>();
        values.add("v");
        requestContext.getHeaders().put("h", values);

        ContextObject copy = requestContext.copyForPropagation();
        copy.getHeaders().get("h").add("injected");
        copy.getHeaders().put("h2", new ArrayList<>());

        Assertions.assertFalse(requestContext.getHeaders().get("h").contains("injected"));
        Assertions.assertFalse(requestContext.getHeaders().containsKey("h2"));
    }

    private static class HeaderfulContext extends ContextObject {
        HeaderfulContext() {
            this.headers = new HashMap<>();
        }
    }

    private static class RedirectableContext extends ContextObject {
        RedirectableContext() {
            this.redirectStartNodes = new ArrayList<>();
        }
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
