package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;

import java.net.URL;

import static dev.aikido.agent_api.helpers.url.PortParser.getPortFromURL;

public final class URLCollector {
    private URLCollector() {}
    public static void report(URL url) {
        ThreadCacheObject threadCache = ThreadCache.get();
        if(threadCache != null) {
            int port = getPortFromURL(url);
            threadCache.getHostnames().add(url.getHost(), port);
            ThreadCache.set(threadCache);
        }
    }
}
