package dev.aikido.agent_api.thread_cache;

public class ThreadCache {
    static final ThreadLocal<ThreadCacheObject> threadCache = new ThreadLocal<>();
    public static ThreadCacheObject get() {
        return threadCache.get();
    }
    public static void set(ThreadCacheObject threadCacheObject) {
        threadCache.set(threadCacheObject);
    }
    public static void reset() {
        threadCache.remove();
    }
}
