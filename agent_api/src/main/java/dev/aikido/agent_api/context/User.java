package dev.aikido.agent_api.context;

import java.io.Serializable;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public record User(String id, String name, String lastIpAddress, long firstSeenAt, long lastSeenAt) implements Serializable {
    public User(User existingUser, long lastSeenAt) {
        this(
            existingUser.id(), existingUser.name(), existingUser.lastIpAddress(),
            existingUser.firstSeenAt(), lastSeenAt
        );
    }
    public User(String id, String name, String lastIpAddress, long time) {
        this(id, name, lastIpAddress, time, time);

    }
}
