package dev.aikido.agent_api.thread_cache;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static dev.aikido.agent_api.thread_cache.ThreadCacheRenewal.renewThreadCache;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

public final class ThreadCache {
    private static final Logger logger = LogManager.getLogger(ThreadCache.class);

    private ThreadCache() {}

    static final long timeToLiveMS = 60 * 1000; // 60 seconds
    static final ThreadLocal<ThreadCacheObject> threadCache = new ThreadLocal<>();

    public static ThreadCacheObject get() {
        return get(/* shouldFetch : */ true); // Default option is to fetch a new config.
    }

    public static ThreadCacheObject get(boolean shouldFetch) {
        ThreadCacheObject currentThreadCache = threadCache.get();
        // If the cache does not yet exist :
        if (currentThreadCache == null && shouldFetch) {
            // Renew/fetch the cache :
            ThreadCacheObject fetchedCache = renewThreadCache();
            set(fetchedCache);
            return fetchedCache;
        }

        // The cache exists already, but we now want to check if TTL is in order :
        if (currentThreadCache != null) {
            long timeElapsedSinceLastSync = getUnixTimeMS() - currentThreadCache.getLastRenewedAtMS();
            if (timeElapsedSinceLastSync <= timeToLiveMS) {
                // TTL is fine, return current thread cache
                return currentThreadCache;
            }
            // TTL exceeded, reset the cache.
            logger.debug("TTL exceeded on a Thread Cache, renewing...");
            if (shouldFetch) {
                ThreadCacheObject fetchedCache = renewThreadCache(currentThreadCache);
                set(fetchedCache);
                return fetchedCache;
            } else {
                reset(); // If the TTL is expired but we're not allowed to fetch new cache we reset.
            }
        }

        return currentThreadCache;
    }

    public static void set(ThreadCacheObject threadCacheObject) {
        if (threadCacheObject == null) {
            reset();
        } else {
            logger.trace("Thread cache initialized or updated.");
            threadCache.set(threadCacheObject);
        }
    }

    public static void reset() {
        threadCache.remove();
    }
}
