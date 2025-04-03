package storage;

import dev.aikido.agent_api.storage.RateLimiterStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimiterStoreTest {

    @BeforeEach
    public void setUp() {
        RateLimiterStore.clear();
    }

    @Test
    public void testIsAllowedWithinLimit() throws InterruptedException {
        String key = "testKey";
        long windowSizeInMS = 60000; // 1 minute
        long maxRequests = 5;

        for (int i = 0; i < 5; i++) {
            assertTrue(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));
        }

        // The 6th request should be rate-limited
        assertFalse(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));
    }

    @Test
    public void testIsAllowedAfterWindowExpires() throws InterruptedException {
        String key = "testKey";
        long windowSizeInMS = 1000; // 1 second
        long maxRequests = 2;

        assertTrue(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));
        assertTrue(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));
        assertFalse(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));

        // Wait for the window to expire
        Thread.sleep(windowSizeInMS);

        // Should be allowed again after the window expires
        assertTrue(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));
    }

    @Test
    public void testClearRateLimiter() {
        String key = "testKey";
        long windowSizeInMS = 60000; // 1 minute
        long maxRequests = 1;

        assertTrue(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));
        assertFalse(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));

        RateLimiterStore.clear();

        // After clearing, the request should be allowed again
        assertTrue(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));
    }

    @Test
    public void testMultipleKeys() {
        String key1 = "testKey1";
        String key2 = "testKey2";
        long windowSizeInMS = 60000; // 1 minute
        long maxRequests = 1;

        assertTrue(RateLimiterStore.isAllowed(key1, windowSizeInMS, maxRequests));
        assertFalse(RateLimiterStore.isAllowed(key1, windowSizeInMS, maxRequests));

        assertTrue(RateLimiterStore.isAllowed(key2, windowSizeInMS, maxRequests));
        assertFalse(RateLimiterStore.isAllowed(key2, windowSizeInMS, maxRequests));
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        String key = "testKey";
        long windowSizeInMS = 1000; // 1 second
        long maxRequests = 2;

        Runnable task = () -> {
            for (int i = 0; i < 3; i++) {
                RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests);
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // After concurrent requests, the rate limiter should still function correctly
        assertFalse(RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests));
    }
}
