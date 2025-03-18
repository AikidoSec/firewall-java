package dev.aikido.agent_api.background.users;

import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class UsersStore {
    private static final Logger logger = LogManager.getLogger(UsersStore.class);
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Users users = new Users();

    private UsersStore() {

    }

    public static List<User> getUsersAsList() {
        mutex.lock();
        List<User> result = users.asList();
        mutex.unlock();
        return result;
    }

    public static void addUser(User user) {
        mutex.lock();
        try {
            users.addUser(user);
        } catch (Throwable e) {
            logger.debug("Error occurred while adding user: %s", e.getMessage());
        }
        mutex.unlock();
    }

    public static void clear() {
        mutex.lock();
        users.clear();
        mutex.unlock();
    }
}
