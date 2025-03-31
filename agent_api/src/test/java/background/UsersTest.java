package background;

import dev.aikido.agent_api.storage.Users;
import dev.aikido.agent_api.context.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsersTest {
    private Users users;

    @BeforeEach
    void setUp() {
        users = new Users(3); // Set max entries to 3 for testing
    }

    @Test
    void testAddUser() {
        User user1 = new User("1", "User One", "192.168.1.1", System.currentTimeMillis(), System.currentTimeMillis());
        users.addUser(user1);

        List<User> userList = users.asList();
        assertEquals(1, userList.size());
        assertEquals(user1, userList.get(0));
    }

    @Test
    void testAddMultipleUsers() {
        User user1 = new User("1", "User One", "192.168.1.1", System.currentTimeMillis(), System.currentTimeMillis());
        User user2 = new User("2", "User Two", "192.168.1.2", System.currentTimeMillis(), System.currentTimeMillis());
        users.addUser(user1);
        users.addUser(user2);

        List<User> userList = users.asList();
        assertEquals(2, userList.size());
        assertTrue(userList.contains(user1));
        assertTrue(userList.contains(user2));
    }

    @Test
    void testMaxEntriesLimit() {
        User user1 = new User("1", "User One", "192.168.1.1", System.currentTimeMillis(), System.currentTimeMillis());
        User user2 = new User("2", "User Two", "192.168.1.2", System.currentTimeMillis(), System.currentTimeMillis());
        User user3 = new User("3", "User Three", "192.168.1.3", System.currentTimeMillis(), System.currentTimeMillis());
        User user4 = new User("4", "User Four", "192.168.1.4", System.currentTimeMillis(), System.currentTimeMillis());

        users.addUser(user1);
        users.addUser(user2);
        users.addUser(user3);
        users.addUser(user4); // This should cause the first user (user1) to be removed

        List<User> userList = users.asList();
        assertEquals(3, userList.size());
        assertFalse(userList.contains(user1)); // user1 should be removed
        assertTrue(userList.contains(user2));
        assertTrue(userList.contains(user3));
        assertTrue(userList.contains(user4));
    }

    @Test
    void testUpdateExistingUser() {
        User user1 = new User("1", "User One", "192.168.1.1", System.currentTimeMillis(), System.currentTimeMillis());
        users.addUser(user1);

        long newLastSeenAt = System.currentTimeMillis();
        User updatedUser1 = new User(user1, newLastSeenAt); // Update the last seen time
        users.addUser(updatedUser1); // This should update the existing user

        List<User> userList = users.asList();
        assertEquals(1, userList.size());
        assertEquals(updatedUser1, userList.get(0)); // Should be the updated user
    }

    @Test
    void testOnlyUpdatesLastSeen() {
        User user1 = new User("1", "User One", "192.168.1.1", 10, 20);
        users.addUser(user1);
        User user2 = new User("1", "User One - New", "192.168.1.1", 30, 500);

        users.addUser(user2); // This should update the existing user

        List<User> userList = users.asList();
        assertEquals(1, userList.size());
        assertEquals("User One", userList.get(0).name());
        assertEquals(10, userList.get(0).firstSeenAt());
        assertEquals(500, userList.get(0).lastSeenAt());
    }

    @Test
    void testClearUsers() {
        User user1 = new User("1", "User One", "192.168.1.1", System.currentTimeMillis(), System.currentTimeMillis());
        users.addUser(user1);
        users.clear();

        List<User> userList = users.asList();
        assertTrue(userList.isEmpty()); // Should be empty after clearing
    }

    @Test
    void testEmptyConstructor() {
        users = new Users(); // Set max entries to default (1000)
        User user1 = new User("2000", "User One", "192.168.1.1", System.currentTimeMillis(), System.currentTimeMillis());
        User user2 = new User("2200", "User Two", "192.168.1.2", System.currentTimeMillis(), System.currentTimeMillis());
        users.addUser(user1);
        users.addUser(user2);
        for (int i = 0; i < (1000 - 1); i++) {
            User userI = new User(String.valueOf(i), "User", "192.168.1.2", System.currentTimeMillis(), System.currentTimeMillis());
            users.addUser(userI);
        }

        List<User> userList = users.asList();
        assertEquals(1000, userList.size());
        assertFalse(userList.contains(user1)); // user1 should be removed
        assertTrue(userList.contains(user2));
    }
}
