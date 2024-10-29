package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.Endpoint;

import java.util.List;
import java.util.Set;

public class ThreadCacheObject {
    private final List<Endpoint> endpoints;
    private final Set<String> blockedUserIds;
    public ThreadCacheObject(Endpoint[] endpoints, String[] blockedUserIDs) {
        // Set endpoints :
        if (endpoints != null) {
            this.endpoints = List.of(endpoints);
        } else {
            this.endpoints = List.of();
        }

        // Set blocked user IDs :
        if (blockedUserIDs != null) {
            this.blockedUserIds = Set.of(blockedUserIDs);
        } else {
            this.blockedUserIds = Set.of();
        }
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }
    public boolean isBlockedUserID(String userID) {
        return blockedUserIds.contains(userID);
    }
}
