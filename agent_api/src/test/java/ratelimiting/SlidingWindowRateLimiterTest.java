package ratelimiting;

import dev.aikido.agent_api.ratelimiting.SlidingWindowRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowRateLimiterTest {
    private SlidingWindowRateLimiter rateLimiter;
    private final int maxItems = 10;
    private final long timeToLiveInMs = 1000; // 1s TTL
    private final long windowSizeInMs = 500; // 500 ms window
    private final long maxRequests = 5; // Allow 5 requests in the window

    @BeforeEach
    void setUp() {
        rateLimiter = new SlidingWindowRateLimiter(maxItems, timeToLiveInMs);
    }

    @Test
    void testAllowRequestsWithinLimit() {
        String key = "user1";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key, 5000000, maxRequests));
        }
    }

    @Test
    void testDenyRequestsExceedingLimit() {
        String key = "user2";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(key, windowSizeInMs, maxRequests);
        }
        // The 6th request should be denied
        assertFalse(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testClearOldEntries() throws InterruptedException {
        String key = "user3";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(key, windowSizeInMs, maxRequests);
        }

        // Sleep to allow old entries to be cleared
        TimeUnit.MILLISECONDS.sleep(windowSizeInMs + 100); // Add a small buffer

        // After sleeping, we should be able to make a new request
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testMultipleKeys() {
        String key1 = "user4";
        String key2 = "user5";

        // Allow requests for key1
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key1, windowSizeInMs, maxRequests));
        }

        // Allow requests for key2
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key2, windowSizeInMs, maxRequests));
        }

        // Deny request for key1 after limit
        assertFalse(rateLimiter.isAllowed(key1, windowSizeInMs, maxRequests));
        // Deny request for key2 after limit
        assertFalse(rateLimiter.isAllowed(key2, windowSizeInMs, maxRequests));
    }

    @Test
    void testTTLExpiration() throws InterruptedException {
        String key = "user6";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(key, windowSizeInMs, maxRequests);
        }

        // Sleep to allow the TTL to expire
        TimeUnit.MILLISECONDS.sleep(timeToLiveInMs + 100); // Add a small buffer

        // After TTL expiration, we should be able to make a new request
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testAllowRequestsExactlyAtLimit() {
        String key = "user7";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
        }
        // The next request should be denied as it hits the limit
        assertFalse(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testAllowRequestsAfterClearingOldEntries() throws InterruptedException {
        String key = "user8";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(key, windowSizeInMs, maxRequests);
        }

        // Sleep to allow old entries to be cleared
        TimeUnit.MILLISECONDS.sleep(windowSizeInMs + 100); // Add a small buffer

        // After clearing, we should be able to make a new request
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testMultipleRapidRequests() throws InterruptedException {
        String key = "user9";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
        }

        // Sleep for a short time to simulate rapid requests
        TimeUnit.MILLISECONDS.sleep(100); // Sleep for 100 ms

        // The next request should be denied as it exceeds the limit
        assertFalse(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testResetAfterTTL() throws InterruptedException {
        String key = "user10";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(key, windowSizeInMs, maxRequests);
        }

        // Sleep to allow the TTL to expire
        TimeUnit.MILLISECONDS.sleep(timeToLiveInMs + 100); // Add a small buffer

        // After TTL expiration, we should be able to make a new request
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testDifferentWindowSizes() {
        String key = "user11";
        long differentWindowSize = 1000; // 1 second window
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key, differentWindowSize, maxRequests));
        }
        // The 6th request should be denied
        assertFalse(rateLimiter.isAllowed(key, differentWindowSize, maxRequests));
    }

    @Test
    void testSlidingWindowWithIntermittentRequests() throws InterruptedException {
        String key = "user14";

        // Allow 5 requests in a 1-second window
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
            TimeUnit.MILLISECONDS.sleep(100); // Sleep 100 ms between requests
        }

        // Sleep for 600 ms to allow the first requests to slide out of the window
        TimeUnit.MILLISECONDS.sleep(600);

        // Now we should be able to make a new request
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testSlidingWindowEdgeCase() throws InterruptedException {
        String key = "user15";

        // Allow 5 requests in a 1-second window
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
        }

        // Sleep for 500 ms to simulate time passing
        TimeUnit.MILLISECONDS.sleep(windowSizeInMs+100);

        // The next request should still be allowed as the window is sliding
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));

        // Sleep for another 500 ms to allow the first batch to expire
        TimeUnit.MILLISECONDS.sleep(windowSizeInMs+100);

        // Now we should be able to make a new request
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testSlidingWindowWithBurstRequests() throws InterruptedException {
        String key = "user16";

        // Allow 5 requests in a 1-second window
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
        }

        // Sleep for 200 ms to simulate time passing
        TimeUnit.MILLISECONDS.sleep(windowSizeInMs/2);
        // add 3 more requests :
        assertFalse(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
        assertFalse(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
        assertFalse(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
        TimeUnit.MILLISECONDS.sleep(windowSizeInMs/2 + 50); // 50ms buffer, first requests are still in window

        // Make a burst of requests (should be allowed)
        for (int i = 0; i < 2; i++) {
            assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
        }
        assertFalse(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));

        // Sleep for 500 ms to allow all batches to slide out window
        TimeUnit.MILLISECONDS.sleep(windowSizeInMs+100);

        // Now we should be able to make a new request
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

    @Test
    void testSlidingWindowWithDelayedRequests() throws InterruptedException {
        String key = "user17";

        // Allow 5 requests in a 1-second window
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
            TimeUnit.MILLISECONDS.sleep(100); // Sleep 100 ms between requests
        }

        // Sleep for 600 ms to allow the first requests to slide out of the window
        TimeUnit.MILLISECONDS.sleep(600);

        // Now we should be able to make a new request
        assertTrue(rateLimiter.isAllowed(key, windowSizeInMs, maxRequests));
    }

}
