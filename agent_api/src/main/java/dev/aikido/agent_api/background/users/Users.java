package dev.aikido.agent_api.background.users;

import dev.aikido.agent_api.context.User;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that holds users for the background process
 */
public class Users {
    private final int maxEntries;
    private final Map<String, User> users;

    public Users(int maxEntries) {
        this.maxEntries = maxEntries;
        this.users = new LinkedHashMap<>();
    }

    public Users() {
        this(1000); // Default max entries
    }

    public void addUser(User user) {
        User existing = users.get(user.id());
        if (existing != null) {
            // Update last seen at:
            users.put(user.id(), new User(existing, user.lastSeenAt()));
            return;
        }
        if (users.size() >= maxEntries) {
            // Remove the first added user (FIFO)
            String firstAddedKey = users.keySet().iterator().next();
            users.remove(firstAddedKey);
        }
        users.put(user.id(), user);
    }

    public List<User> asList() {
        return users.values().stream().toList();
    }

    public void clear() {
        users.clear();
    }
}
