package dev.aikido.agent_api.thread_cache;

public class ThreadCacheRenewal {
    public static ThreadCacheObject renewThreadCache() {
        // Fetch thread cache over IPC:
        // ...
        return new ThreadCacheObject(null, null);
    }
}
