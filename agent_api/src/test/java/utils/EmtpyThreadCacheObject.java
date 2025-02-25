package utils;

import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EmtpyThreadCacheObject {
    public static ThreadCacheObject getEmptyThreadCacheObject() {
        return new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes(), Optional.empty());
    }
    public static ThreadCacheObject getEmptyThreadCacheObject(Set<String> bypassedIps) {
        return new ThreadCacheObject(List.of(), Set.of(), bypassedIps, new Routes(), Optional.empty());
    }
}
