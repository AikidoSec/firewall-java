package background;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceConfigurationTest {

    private ServiceConfiguration serviceConfiguration;

    @BeforeEach
    void setUp() {
        serviceConfiguration = new ServiceConfiguration(true, "someServerless");
    }

    @Test
    void constructor_ShouldThrowException_WhenServerlessIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new ServiceConfiguration(true, "");
        });
        assertEquals("Serverless cannot be an empty string", exception.getMessage());
    }

    @Test
    void constructor_ShouldInitializeFieldsCorrectly() {
        assertTrue(serviceConfiguration.isBlockingEnabled());
        assertEquals("someServerless", serviceConfiguration.getServerless());
        assertTrue(serviceConfiguration.getBypassedIPs().isEmpty());
        assertTrue(serviceConfiguration.getBlockedUserIDs().isEmpty());
        assertTrue(serviceConfiguration.getEndpoints().isEmpty());
    }

    @Test
    void updateConfig_ShouldNotChangeState_WhenApiResponseIsNull() {
        serviceConfiguration.updateConfig(null);
        assertTrue(serviceConfiguration.isBlockingEnabled());
        assertTrue(serviceConfiguration.getBypassedIPs().isEmpty());
        assertTrue(serviceConfiguration.getBlockedUserIDs().isEmpty());
        assertTrue(serviceConfiguration.getEndpoints().isEmpty());
    }

    @Test
    void updateConfig_ShouldNotChangeState_WhenApiResponseIsUnsuccessful() {
        APIResponse apiResponse = new APIResponse(false, null, 0, null, null, null, false, false);
        serviceConfiguration.updateConfig(apiResponse);
        assertTrue(serviceConfiguration.isBlockingEnabled());
        assertTrue(serviceConfiguration.getBypassedIPs().isEmpty());
        assertTrue(serviceConfiguration.getBlockedUserIDs().isEmpty());
        assertTrue(serviceConfiguration.getEndpoints().isEmpty());
    }

    @Test
    void updateConfig_ShouldUpdateBlockingEnabled_WhenApiResponseIsSuccessful() {
        APIResponse apiResponse = new APIResponse(true, null, 0, null, null, null, false, false);
        serviceConfiguration.updateConfig(apiResponse);
        assertFalse(serviceConfiguration.isBlockingEnabled());
    }

    @Test
    void updateConfig_ShouldUpdateBypassedIPs_WhenApiResponseHasAllowedIPs() {
        APIResponse apiResponse = new APIResponse(true, null, 0, null, null, Arrays.asList("192.168.1.1", "192.168.1.2"), false, false);
        serviceConfiguration.updateConfig(apiResponse);
        assertEquals(new HashSet<>(Arrays.asList("192.168.1.1", "192.168.1.2")), serviceConfiguration.getBypassedIPs());
    }

    @Test
    void updateConfig_ShouldUpdateBlockedUserIDs_WhenApiResponseHasBlockedUserIds() {
        APIResponse apiResponse = new APIResponse(true, null, 0, null, Arrays.asList("user1", "user2"), null, false, false);
        serviceConfiguration.updateConfig(apiResponse);
        assertEquals(new HashSet<>(Arrays.asList("user1", "user2")), serviceConfiguration.getBlockedUserIDs());
    }

    @Test
    void updateConfig_ShouldUpdateEndpoints_WhenApiResponseHasEndpoints() {
        Endpoint endpoint1 = new Endpoint("GET", "/api/test1", 0, 0, List.of(), false, false, false);
        Endpoint endpoint2 = new Endpoint("POST", "/api/test2", 0, 0, List.of(), false, false, false);
        APIResponse apiResponse = new APIResponse(true, null, 0, Arrays.asList(endpoint1, endpoint2), null, null, false, false);
        serviceConfiguration.updateConfig(apiResponse);
        assertEquals(Arrays.asList(endpoint1, endpoint2), serviceConfiguration.getEndpoints());
    }

    @Test
    void updateConfig_ShouldHandleAllUpdates_WhenApiResponseIsComplete() {
        Endpoint endpoint1 = new Endpoint("GET", "/api/test1", 0, 0, List.of(), false, false, false);
        APIResponse apiResponse = new APIResponse(true, null, 0, Arrays.asList(endpoint1), Arrays.asList("user1"), Arrays.asList("192.168.1.1"), false, true);
        serviceConfiguration.updateConfig(apiResponse);
        assertTrue(serviceConfiguration.isBlockingEnabled());
        assertEquals(new HashSet<>(Arrays.asList("user1")), serviceConfiguration.getBlockedUserIDs());
        assertEquals(new HashSet<>(Arrays.asList("192.168.1.1")), serviceConfiguration.getBypassedIPs());
        assertEquals(Arrays.asList(endpoint1), serviceConfiguration.getEndpoints());
    }
}
