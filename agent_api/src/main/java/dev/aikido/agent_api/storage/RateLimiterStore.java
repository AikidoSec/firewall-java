package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.ratelimiting.RateLimiter;
import dev.aikido.agent_api.ratelimiting.SlidingWindowRateLimiter;

import java.util.concurrent.locks.ReentrantLock;

public final class RateLimiterStore {
    private static final Logger logger = LogManager.getLogger(RateLimiterStore.class);
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final RateLimiter rateLimiter = new SlidingWindowRateLimiter(
            /*maxItems:*/ 5000, /*TTL in ms:*/ 120 * 60 * 1000 // 120 minutes
    );

    private RateLimiterStore() {
    }

    public static boolean isAllowed(String key, long windowSizeInMS, long maxRequests) {
        mutex.lock();
        try {
            logger.trace("Checking rate-limiting for: %s", key);
            return rateLimiter.isAllowed(key, windowSizeInMS, maxRequests);
        } catch (Throwable e) {
            logger.debug("Error occurred checking rate-limiting for %s, %s", key, e.getMessage());
            return true;
        } finally {
            mutex.unlock();
        }
    }

    public static void clear() {
        mutex.lock();
        try {
            rateLimiter.clear();
        } catch (Throwable e) {
            logger.debug("Clearing of rate-limiter failed: %s", e.getMessage());
        } finally {
            mutex.unlock();
        }
    }
}
