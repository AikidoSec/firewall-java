package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExecutorWrapperTest {
    @AfterEach
    void tearDown() {
        Context.reset();
    }

    @Test
    void scheduledThreadPoolExecutorSchedulePropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
        ctx.setRateLimitGroup("scheduled");
        Context.set(ctx);
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        try {
            ContextObject onWorker = executor.schedule(Context::get, 0, TimeUnit.MILLISECONDS).get(5, TimeUnit.SECONDS);
            assertEquals("scheduled", onWorker.getRateLimitGroup());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void delegatedSingleThreadExecutorSubmitPropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
        ctx.setRateLimitGroup("delegated");
        Context.set(ctx);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            assertEquals("delegated", executor.submit(Context::get).get(5, TimeUnit.SECONDS).getRateLimitGroup());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void threadPoolExecutorExecutePropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
        ctx.setRateLimitGroup("threadpool");
        Context.set(ctx);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        AtomicReference<ContextObject> onWorker = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        try {
            executor.execute(() -> {
                onWorker.set(Context.get());
                done.countDown();
            });
            done.await(5, TimeUnit.SECONDS);
            assertEquals("threadpool", onWorker.get().getRateLimitGroup());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void forkJoinPoolSubmitPropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
        ctx.setRateLimitGroup("forkjoin");
        Context.set(ctx);
        ForkJoinPool executor = new ForkJoinPool(1);
        try {
            assertEquals("forkjoin", executor.submit(Context::get).get(5, TimeUnit.SECONDS).getRateLimitGroup());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void completableFutureSupplyAsyncPropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
        ctx.setRateLimitGroup("completablefuture");
        Context.set(ctx);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            ContextObject onWorker = CompletableFuture.supplyAsync(Context::get, executor).get(5, TimeUnit.SECONDS);
            assertEquals("completablefuture", onWorker.getRateLimitGroup());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentTasksKeepTheirOwnContext() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier bothRunning = new CyclicBarrier(2);
        Callable<ContextObject> readWhileConcurrent = () -> {
            bothRunning.await(5, TimeUnit.SECONDS);
            return Context.get();
        };
        ContextObject first = new ContextObject();
        first.setRateLimitGroup("first");
        ContextObject second = new ContextObject();
        second.setRateLimitGroup("second");
        try {
            Context.set(first);
            Future<ContextObject> a = executor.submit(readWhileConcurrent);
            Context.set(second);
            Future<ContextObject> b = executor.submit(readWhileConcurrent);
            assertEquals("first", a.get(5, TimeUnit.SECONDS).getRateLimitGroup());
            assertEquals("second", b.get(5, TimeUnit.SECONDS).getRateLimitGroup());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentTasksFromOneContextEachGetTheirOwnCopy() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier bothRunning = new CyclicBarrier(2);
        Callable<ContextObject> readWhileConcurrent = () -> {
            bothRunning.await(5, TimeUnit.SECONDS);
            return Context.get();
        };
        ContextObject ctx = new ContextObject();
        try {
            Context.set(ctx);
            Future<ContextObject> a = executor.submit(readWhileConcurrent);
            Future<ContextObject> b = executor.submit(readWhileConcurrent);
            ContextObject onA = a.get(5, TimeUnit.SECONDS);
            ContextObject onB = b.get(5, TimeUnit.SECONDS);
            assertNotSame(ctx, onA);
            assertNotSame(ctx, onB);
            assertNotSame(onA, onB);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void nestedSubmitPropagatesOuterContext() throws Exception {
        ExecutorService outer = Executors.newSingleThreadExecutor();
        ExecutorService inner = Executors.newSingleThreadExecutor();
        ContextObject ctx = new ContextObject();
        ctx.setRateLimitGroup("outer");
        try {
            Context.set(ctx);
            ContextObject seen = outer.submit(
                    () -> inner.submit(Context::get).get(5, TimeUnit.SECONDS)
            ).get(5, TimeUnit.SECONDS);
            assertEquals("outer", seen.getRateLimitGroup());
        } finally {
            outer.shutdownNow();
            inner.shutdownNow();
        }
    }

    @Test
    void pooledWorkerDoesNotLeakContextToNextTask() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Context.set(new ContextObject());
            executor.submit(Context::get).get(5, TimeUnit.SECONDS);
            Context.reset();
            assertNull(executor.submit(Context::get).get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void taskSubmittedWithoutContextRunsWithNone() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Context.reset();
            assertNull(executor.submit(Context::get).get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }
}
