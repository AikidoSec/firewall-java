package storage;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.storage.ServiceConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ServiceConfigurationTest {

    private ServiceConfiguration serviceConfiguration;

    @BeforeEach
    public void setUp() {
        serviceConfiguration = new ServiceConfiguration();
    }

    @Test
    public void testUpdateConfig() {
        APIResponse apiResponse = new APIResponse(
                true,
                null,
                12345L,
                List.of(mock(Endpoint.class)),
                List.of("user1", "user2"),
                List.of("192.168.1.1"),
                true,
                true
        );

        serviceConfiguration.updateConfig(apiResponse);

        assertTrue(serviceConfiguration.isBlockingEnabled());
        assertTrue(serviceConfiguration.hasReceivedAnyStats());
        assertEquals(1, serviceConfiguration.getEndpoints().size());
        assertTrue(serviceConfiguration.isUserBlocked("user1"));
        assertTrue(serviceConfiguration.isIpBypassed("192.168.1.1"));
    }

    @Test
    public void testUpdateConfigWithNullResponse() {
        serviceConfiguration.updateConfig(null);
        assertFalse(serviceConfiguration.isBlockingEnabled());
    }

    @Test
    public void testUpdateConfigWithUnsuccessfulResponse() {
        APIResponse apiResponse = new APIResponse(
                false,
                "Error",
                0L,
                null,
                null,
                null,
                false,
                false
        );

        serviceConfiguration.updateConfig(apiResponse);
        assertFalse(serviceConfiguration.isBlockingEnabled());
    }

    @Test
    public void testIsIpBlocked() {
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                Collections.emptyList(),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.1");
        assertTrue(result.blocked());
        assertEquals("blocked", result.description());
    }

    @Test
    public void testIsIpAllowed() {
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry("source", "allowed", List.of("192.168.1.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                Collections.emptyList(),
                List.of(allowedEntry),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.1");
        assertFalse(result.blocked());
    }

    @Test
    public void testIsBlockedUserAgent() {
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                "blocked-agent"
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertTrue(serviceConfiguration.isBlockedUserAgent("blocked-agent"));
        assertFalse(serviceConfiguration.isBlockedUserAgent("allowed-agent"));
    }

    @Test
    public void testMiddlewareInstalled() {
        assertFalse(serviceConfiguration.isMiddlewareInstalled());
        serviceConfiguration.setMiddlewareInstalled(true);
        assertTrue(serviceConfiguration.isMiddlewareInstalled());
    }


    @Test
    public void testIsIpBlockedWithPrivateIp() {
        // Private IPs should never be blocked
        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("127.0.0.1");
        assertFalse(result.blocked());
    }

    @Test
    public void testIsIpBlockedWithEmptyAllowedList() {
        // If allowed IPs list is empty, any IP should not be blocked by allowed list logic
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.1");
        assertFalse(result.blocked());
    }

    @Test
    public void testIsIpBlockedWithMultipleBlockedEntries() {
        ReportingApi.ListsResponseEntry blockedEntry1 = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.1"));
        ReportingApi.ListsResponseEntry blockedEntry2 = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.2"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry1, blockedEntry2),
                Collections.emptyList(),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result1 = serviceConfiguration.isIpBlocked("192.168.1.1");
        ServiceConfiguration.BlockedResult result2 = serviceConfiguration.isIpBlocked("192.168.1.2");
        assertTrue(result1.blocked());
        assertTrue(result2.blocked());
    }

    @Test
    public void testIsIpBlockedWithMixedAllowedAndBlockedEntries() {
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry("source", "allowed", List.of("192.168.1.1"));
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.2"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                List.of(allowedEntry),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult resultAllowed = serviceConfiguration.isIpBlocked("192.168.1.1");
        ServiceConfiguration.BlockedResult resultBlocked = serviceConfiguration.isIpBlocked("192.168.1.2");
        assertFalse(resultAllowed.blocked());
        assertTrue(resultBlocked.blocked());
    }

    @Test
    public void testIsBlockedUserAgentWithNullRegex() {
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertFalse(serviceConfiguration.isBlockedUserAgent("any-agent"));
    }

    @Test
    public void testIsBlockedUserAgentWithCaseInsensitiveMatch() {
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                "blocked-agent"
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertTrue(serviceConfiguration.isBlockedUserAgent("BLOCKED-AGENT"));
    }

    @Test
    public void testBlockingEnabled() {
        assertFalse(serviceConfiguration.isBlockingEnabled());
        serviceConfiguration.setBlocking(true);
        assertTrue(serviceConfiguration.isBlockingEnabled());
    }

    @Test
    public void testReceivedAnyStats() {
        assertTrue(serviceConfiguration.hasReceivedAnyStats());
        APIResponse apiResponse = new APIResponse(
                true,
                null,
                12345L,
                null,
                null,
                null,
                false,
                true
        );

        serviceConfiguration.updateConfig(apiResponse);
        assertFalse(serviceConfiguration.hasReceivedAnyStats());
    }

    @Test
    public void testIsIpBypassedWithEmptyBypassedList() {
        // If bypassed IPs list is empty, no IP should be bypassed
        serviceConfiguration.updateConfig(new APIResponse(
                true,
                null,
                12345L,
                null,
                null,
                Collections.emptyList(),
                true,
                true
        ));

        assertFalse(serviceConfiguration.isIpBypassed("192.168.1.1"));
    }

    @Test
    public void testIsIpBypassedWithMultipleBypassedEntries() {
        APIResponse apiResponse = new APIResponse(
                true,
                null,
                12345L,
                null,
                null,
                List.of("192.168.1.1", "192.168.1.2"),
                true,
                true
        );

        serviceConfiguration.updateConfig(apiResponse);

        assertTrue(serviceConfiguration.isIpBypassed("192.168.1.1"));
        assertTrue(serviceConfiguration.isIpBypassed("192.168.1.2"));
    }

    @Test
    public void testIsUserBlockedWithEmptyBlockedUserList() {
        // If blocked user IDs list is empty, no user should be blocked
        serviceConfiguration.updateConfig(new APIResponse(
                true,
                null,
                12345L,
                null,
                Collections.emptyList(),
                null,
                true,
                true
        ));

        assertFalse(serviceConfiguration.isUserBlocked("user1"));
    }

    @Test
    public void testIsUserBlockedWithMultipleBlockedUsers() {
        APIResponse apiResponse = new APIResponse(
                true,
                null,
                12345L,
                null,
                List.of("user1", "user2"),
                null,
                true,
                true
        );

        serviceConfiguration.updateConfig(apiResponse);

        assertTrue(serviceConfiguration.isUserBlocked("user1"));
        assertTrue(serviceConfiguration.isUserBlocked("user2"));
    }

    @Test
    public void testGetEndpoints() {
        APIResponse apiResponse = new APIResponse(
                true,
                null,
                12345L,
                List.of(mock(Endpoint.class)),
                null,
                null,
                true,
                true
        );

        serviceConfiguration.updateConfig(apiResponse);

        assertEquals(1, serviceConfiguration.getEndpoints().size());
    }

    @Test
    public void testGetEndpointsWithEmptyList() {
        APIResponse apiResponse = new APIResponse(
                true,
                null,
                12345L,
                Collections.emptyList(),
                null,
                null,
                true,
                true
        );

        serviceConfiguration.updateConfig(apiResponse);

        assertTrue(serviceConfiguration.getEndpoints().isEmpty());
    }

    @Test
    public void testIsIpBlockedWithSubnet() {
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.0/24"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                List.of(),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.100");
        assertTrue(result.blocked());
        assertEquals("blocked", result.description());
    }

    @Test
    public void testIsIpBlockedWithMixedSubnetAndSingleIP() {
        ReportingApi.ListsResponseEntry blockedEntry1 = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.0/24"));
        ReportingApi.ListsResponseEntry blockedEntry2 = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("10.0.0.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry1, blockedEntry2),
                List.of(),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result1 = serviceConfiguration.isIpBlocked("192.168.1.100");
        ServiceConfiguration.BlockedResult result2 = serviceConfiguration.isIpBlocked("10.0.0.1");
        assertTrue(result1.blocked());
        assertTrue(result2.blocked());
    }

    @Test
    public void testIsIpBlockedWithEmptyBlockedList() {
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(),
                List.of(),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.1");
        assertFalse(result.blocked());
    }

    @Test
    public void testIsBlockedUserAgentWithEmptyRegex() {
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(),
                List.of(),
                ""
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertFalse(serviceConfiguration.isBlockedUserAgent("any-agent"));
    }

    @Test
    public void testIsBlockedUserAgentWithComplexRegex() {
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(),
                List.of(),
                "blocked.*agent|another.*pattern"
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertTrue(serviceConfiguration.isBlockedUserAgent("blocked-agent"));
        assertTrue(serviceConfiguration.isBlockedUserAgent("another-pattern"));
        assertFalse(serviceConfiguration.isBlockedUserAgent("allowed-agent"));
    }

    @Test
    public void testIsIpBlockedWithAllowedAndBlockedIPs() {
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry("source", "allowed", List.of("10.0.0.1"));
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                List.of(allowedEntry),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult resultAllowed = serviceConfiguration.isIpBlocked("10.0.0.1");
        ServiceConfiguration.BlockedResult resultBlocked = serviceConfiguration.isIpBlocked("192.168.1.1");
        assertFalse(resultAllowed.blocked());
        assertTrue(resultBlocked.blocked());
    }

    @Test
    public void testIsIpBlockedWithOnlyAllowedIPs() {
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry("source", "allowed", List.of("10.0.0.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(),
                List.of(allowedEntry),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult resultAllowed = serviceConfiguration.isIpBlocked("10.0.0.1");
        ServiceConfiguration.BlockedResult resultNotAllowed = serviceConfiguration.isIpBlocked("2.2.2.2");
        assertFalse(resultAllowed.blocked());
        assertTrue(resultNotAllowed.blocked());
    }

    @Test
    public void testIsIpBlockedWithOnlyBlockedIPs() {
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                List.of(),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult resultBlocked = serviceConfiguration.isIpBlocked("192.168.1.1");
        ServiceConfiguration.BlockedResult resultNotBlocked = serviceConfiguration.isIpBlocked("10.0.0.1");
        assertTrue(resultBlocked.blocked());
        assertFalse(resultNotBlocked.blocked());
    }

    @Test
    public void testIsIpBlockedWithAllowedIPsAndBlockedIPs() {
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry("source", "allowed", List.of("10.0.0.1"));
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry("source", "blocked", List.of("192.168.1.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                List.of(allowedEntry),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult resultAllowed = serviceConfiguration.isIpBlocked("10.0.0.1");
        ServiceConfiguration.BlockedResult resultBlocked = serviceConfiguration.isIpBlocked("192.168.1.1");
        ServiceConfiguration.BlockedResult resultNotAllowedLocal = serviceConfiguration.isIpBlocked("192.168.1.2");
        assertFalse(resultAllowed.blocked());
        assertTrue(resultBlocked.blocked());
        assertFalse(resultNotAllowedLocal.blocked());
    }
}
