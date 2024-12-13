package thread_cache;


import dev.aikido.agent_api.background.utilities.ThreadClient;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadCacheTest {

    private ThreadClient mockClient;

    @BeforeEach
    public void setUp() {
        mockClient = Mockito.mock(ThreadClient.class);
        //IPCDefaultClient.setClient(mockClient); // Assuming you have a way to set the client
    }

    @Test
    public void testGetCacheWhenNotExpired() {
        // Arrange
        Set<String> blockedUserIds = Set.of("user1", "user2");
        Routes routes = new Routes(); // Assuming Routes has a default constructor
        ThreadCacheObject expectedCache = new ThreadCacheObject(Collections.emptyList(), blockedUserIds, Set.of(), routes);
        ThreadCache.set(expectedCache);

        // Act
        ThreadCacheObject actualCache = ThreadCache.get();

        // Assert
        assertNotNull(actualCache);
        assertEquals(expectedCache.getEndpoints(), actualCache.getEndpoints());
    }
/*
    @Test
    public void testGetCacheWhenExpired() throws InterruptedException {
        // Arrange
        long timeToLiveMS = 60 * 1000; // 60 seconds
        ThreadCacheObject expiredCache = new ThreadCacheObject(Collections.emptyList(), Set.of(), new Routes());
        ThreadCache.set(expiredCache);

        // Simulate time passing
        Thread.sleep(timeToLiveMS + 1);

        // Mock the ThreadClient to return a new cache
        SyncDataCommand.SyncDataResult result = new SyncDataCommand.SyncDataResult(Collections.emptyList(), Set.of("user3"), new Routes());
        String jsonResult = new Gson().toJson(result);
        when(mockClient.sendData("SYNC_DATA$", true)).thenReturn(Optional.of(jsonResult));

        // Act
        ThreadCacheObject actualCache = ThreadCache.get();

        // Assert
        assertNotNull(actualCache);
        assertTrue(actualCache.isBlockedUserID("user3"));
    }

    @Test
    public void testRenewThreadCacheReturnsNull() {
        // Arrange
        when(mockClient.sendData("SYNC_DATA$", true)).thenReturn(Optional.empty());

        // Act
        ThreadCacheObject actualCache = ThreadCacheRenewal.renewThreadCache();

        // Assert
        assertNull(actualCache);
    }

    @Test
    public void testThreadCacheObjectInitialization() {
        // Arrange
        Set<String> blockedUserIds = Set.of("user1");
        Routes routes = new Routes(); // Assuming Routes has a default constructor

        // Act
        ThreadCacheObject cacheObject = new ThreadCacheObject(Collections.emptyList(), blockedUserIds, routes);

        // Assert
        assertEquals(Collections.emptyList(), cacheObject.getEndpoints());
        assertNotNull(cacheObject.getHostnames());
        assertNotNull(cacheObject.getRoutes());
    }
 */
}