package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;

import java.net.URL;

public final class URLCollector {
    private URLCollector() {}
    public static void report(URL url) {
        ThreadCacheObject threadCache = ThreadCache.get();
        if(threadCache != null) {
            threadCache.getHostnames().add(url.getHost(), url.getPort());
            ThreadCache.set(threadCache);
        }
    }
}
