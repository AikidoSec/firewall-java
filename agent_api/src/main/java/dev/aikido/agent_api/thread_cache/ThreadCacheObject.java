package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.Endpoint;

import java.util.List;
import java.util.Set;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class ThreadCacheObject {
    private final List<Endpoint> endpoints;
    private final Set<String> blockedUserIds;
    private final long lastRenewedAtMS;
    public ThreadCacheObject(Endpoint[] endpoints, String[] blockedUserIDs) {
        this.lastRenewedAtMS = getUnixTimeMS();
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

    public long getLastRenewedAtMS() {
        return lastRenewedAtMS;
    }
}
