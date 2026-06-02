package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;

class ExecutorWrapperTest {
    @AfterEach
    void tearDown() {
        Context.reset();
    }

    @Test
    void scheduledThreadPoolExecutorSchedulePropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
        Context.set(ctx);
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        try {
            assertSame(ctx, executor.schedule(Context::get, 0, TimeUnit.MILLISECONDS).get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void delegatedSingleThreadExecutorSubmitPropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
        Context.set(ctx);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            assertSame(ctx, executor.submit(Context::get).get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void threadPoolExecutorExecutePropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
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
            assertSame(ctx, onWorker.get());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void forkJoinPoolSubmitPropagatesContext() throws Exception {
        ContextObject ctx = new ContextObject();
        Context.set(ctx);
        ForkJoinPool executor = new ForkJoinPool(1);
        try {
            assertSame(ctx, executor.submit(Context::get).get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }
}
