package ratelimiting;

import dev.aikido.agent_api.ratelimiting.RateLimiter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void testAllowUpToMaxAmountRequestsWithinTTL() {
        int maxAmount = 5;
        long ttl = 500; // 0.5 seconds
        RateLimiter rateLimiter = new RateLimiter(maxAmount, ttl);

        String key = "user1";
        for (int i = 0; i < maxAmount; i++) {
            assertTrue(rateLimiter.isAllowed(key, ttl, maxAmount),
                "Request " + (i + 1) + " should be allowed");
        }

        assertFalse(rateLimiter.isAllowed(key, ttl, maxAmount),
            "Request 6 should not be allowed");
    }

    @Test
    void testResetAfterTTLExpired() throws InterruptedException {
        int maxAmount = 5;
        long ttl = 500; // 0.5 seconds
        RateLimiter rateLimiter = new RateLimiter(maxAmount, ttl);

        String key = "user1";
        for (int i = 0; i < maxAmount; i++) {
            assertTrue(rateLimiter.isAllowed(key, ttl, maxAmount),
                "Request " + (i + 1) + " should be allowed");
        }

        assertFalse(rateLimiter.isAllowed(key, ttl, maxAmount),
            "Request 6 should not be allowed");

        // Simulate the passage of time
        TimeUnit.MILLISECONDS.sleep(ttl + 100); // Add a small buffer

        assertTrue(rateLimiter.isAllowed(key, ttl, maxAmount),
            "Request after TTL should be allowed");
    }

    @Test
    void testAllowRequestsForDifferentKeysIndependently() {
        int maxAmount = 5;
        long ttl = 500; // 0.5 seconds
        RateLimiter rateLimiter = new RateLimiter(maxAmount, ttl);

        String key1 = "user1";
        String key2 = "user2";

        for (int i = 0; i < maxAmount; i++) {
            assertTrue(rateLimiter.isAllowed(key1, ttl, maxAmount),
                "Request " + (i + 1) + " for key1 should be allowed");
        }

        assertFalse(rateLimiter.isAllowed(key1, ttl, maxAmount),
            "Request 6 for key1 should not be allowed");

        for (int i = 0; i < maxAmount; i++) {
            assertTrue(rateLimiter.isAllowed(key2, ttl, maxAmount),
                "Request " + (i + 1) + " for key2 should be allowed");
        }

        assertFalse(rateLimiter.isAllowed(key2, ttl, maxAmount),
            "Request 6 for key2 should not be allowed");
    }
}
