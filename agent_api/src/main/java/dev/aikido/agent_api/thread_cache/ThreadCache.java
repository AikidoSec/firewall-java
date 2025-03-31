package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public final class ThreadCache {
    private static final Logger logger = LogManager.getLogger(ThreadCache.class);

    private ThreadCache() {}

    static final long timeToLiveMS = 60 * 1000; // 60 seconds
    static final ThreadLocal<ThreadCacheObject> threadCache = new ThreadLocal<>();
    public static ThreadCacheObject get() {
        return get(/* shouldFetch : */ true); // Default option is to fetch a new config.
    }
    public static ThreadCacheObject get(boolean shouldFetch) {
        return threadCache.get();
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
