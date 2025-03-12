package dev.aikido.agent_api.storage;

import java.util.HashSet;
import java.util.Set;

public class Configuration {
    private boolean blocking;
    // This is true by default, awaiting the startup event, if the startup event is unsuccessful this will remain true.
    private boolean receivedAnyStats = true;
    private boolean middlewareInstalled = false;
    private Set<String> blockedUserIds = new HashSet<>();

    public Configuration() {}

    // Blocking status, i.e. should an attack be throwing an error or not
    public boolean isBlocking() {
        return blocking;
    }
    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    // blocked user ids: These are users that were blocked in the cloud portal
    public void setBlockedUserIds(Set<String> blockedUserIds) {
        this.blockedUserIds = blockedUserIds;
    }
    public boolean isUserBlocked(String userId) {
        return this.blockedUserIds.contains(userId);
    }
}
