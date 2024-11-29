package dev.aikido.agent_api.thread_cache;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static dev.aikido.agent_api.thread_cache.ThreadCacheRenewal.renewThreadCache;

public final class ThreadCache {
    private ThreadCache() {}

    static final long timeToLiveMS = 60 * 1000; // 60 seconds
    static final ThreadLocal<ThreadCacheObject> threadCache = new ThreadLocal<>();
    public static ThreadCacheObject get() {
        return get(/* shouldFetch : */ true); // Default option is to fetch a new config.
    }
    public static ThreadCacheObject get(boolean shouldFetch) {
        ThreadCacheObject currentThreadCache = threadCache.get();

        // Check TTL :
        if (currentThreadCache != null) {
            long timeElapsedSinceLastSync = getUnixTimeMS() - currentThreadCache.getLastRenewedAtMS();
            if (timeElapsedSinceLastSync > timeToLiveMS) {
                // TTL exceeded, reset the cache.
                reset();
                currentThreadCache = null;
            }
        }

        // If the cache did not exist or a reset happened, the cache could be null now.
        if (currentThreadCache == null && shouldFetch) {
            // Renew/fetch the cache :
            currentThreadCache = renewThreadCache();
            set(currentThreadCache);
        }
        
        return currentThreadCache;
    }
    public static void set(ThreadCacheObject threadCacheObject) {
        threadCache.set(threadCacheObject);
    }
    public static void reset() {
        threadCache.remove();
    }
}
