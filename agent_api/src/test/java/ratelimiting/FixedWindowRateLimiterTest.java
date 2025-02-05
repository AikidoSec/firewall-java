package ratelimiting;

import dev.aikido.agent_api.ratelimiting.FixedWindowRateLimiter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

class FixedWindowRateLimiterTest {

    @Test
    void testAllowUpToMaxAmountRequestsWithinTTL() {
        int maxAmount = 5;
        long ttl = 500; // 0.5 seconds
        FixedWindowRateLimiter fixedWindowRateLimiter = new FixedWindowRateLimiter(maxAmount, ttl);

        String key = "user1";
        for (int i = 0; i < maxAmount; i++) {
            assertTrue(fixedWindowRateLimiter.isAllowed(key, ttl, maxAmount),
                    "Request " + (i + 1) + " should be allowed");
        }

        assertFalse(fixedWindowRateLimiter.isAllowed(key, ttl, maxAmount),
                "Request 6 should not be allowed");
    }

    @Test
    void testResetAfterTTLExpired() throws InterruptedException {
        int maxAmount = 5;
        long ttl = 500; // 0.5 seconds
        FixedWindowRateLimiter fixedWindowRateLimiter = new FixedWindowRateLimiter(maxAmount, ttl);

        String key = "user1";
        for (int i = 0; i < maxAmount; i++) {
            assertTrue(fixedWindowRateLimiter.isAllowed(key, ttl, maxAmount),
                    "Request " + (i + 1) + " should be allowed");
        }

        assertFalse(fixedWindowRateLimiter.isAllowed(key, ttl, maxAmount),
                "Request 6 should not be allowed");

        // Simulate the passage of time
        TimeUnit.MILLISECONDS.sleep(ttl + 100); // Add a small buffer

        assertTrue(fixedWindowRateLimiter.isAllowed(key, ttl, maxAmount),
                "Request after TTL should be allowed");
    }

    @Test
    void testAllowRequestsForDifferentKeysIndependently() {
        int maxAmount = 5;
        long ttl = 500; // 0.5 seconds
        FixedWindowRateLimiter fixedWindowRateLimiter = new FixedWindowRateLimiter(maxAmount, ttl);

        String key1 = "user1";
        String key2 = "user2";

        for (int i = 0; i < maxAmount; i++) {
            assertTrue(fixedWindowRateLimiter.isAllowed(key1, ttl, maxAmount),
                    "Request " + (i + 1) + " for key1 should be allowed");
        }

        assertFalse(fixedWindowRateLimiter.isAllowed(key1, ttl, maxAmount),
                "Request 6 for key1 should not be allowed");

        for (int i = 0; i < maxAmount; i++) {
            assertTrue(fixedWindowRateLimiter.isAllowed(key2, ttl, maxAmount),
                    "Request " + (i + 1) + " for key2 should be allowed");
        }

        assertFalse(fixedWindowRateLimiter.isAllowed(key2, ttl, maxAmount),
                "Request 6 for key2 should not be allowed");
    }
}
