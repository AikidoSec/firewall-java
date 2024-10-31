package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

public class URLCollector {
    private static final Logger logger = LogManager.getLogger(URLCollector.class);
    public static void report(URL url) {
        ThreadCacheObject threadCache = ThreadCache.get();
        if(threadCache != null) {
            threadCache.getHostnames().add(url.getHost(), url.getPort());
            ThreadCache.set(threadCache);
        }
    }
}
