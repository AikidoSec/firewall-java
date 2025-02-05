package ratelimiting;

import dev.aikido.agent_api.ratelimiting.sliding_window.SlidingWindowRateLimiter;
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
}
