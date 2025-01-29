package dev.aikido.agent_api.context;

public record User(String id, String name, String lastIpAddress, long firstSeenAt, long lastSeenAt) {
    public User(User existingUser, long lastSeenAt) {
        this(
                existingUser.id(),
                existingUser.name(),
                existingUser.lastIpAddress(),
                existingUser.firstSeenAt(),
                lastSeenAt);
    }

    public User(String id, String name, String lastIpAddress, long time) {
        this(id, name, lastIpAddress, time, time);
    }
}
