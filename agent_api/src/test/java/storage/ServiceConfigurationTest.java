package storage;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.storage.ServiceConfiguration;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
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
        StatisticsStore.clear();
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
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry(false, "my-key", "source", "blocked", List.of("192.168.1.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                Collections.emptyList(),
                Collections.emptyList()
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.1");
        assertTrue(result.blocked());
        assertEquals("blocked", result.description());
        assertEquals(1, StatisticsStore.getStatsRecord().ipAddresses().get("my-key").total());
        assertEquals(1, StatisticsStore.getStatsRecord().ipAddresses().get("my-key").blocked());
    }

    @Test
    public void testIsIpBlockedOrMonitored() {
        var blockedEntry = new ReportingApi.ListsResponseEntry(false, "blocked-key", "source", "blocked", List.of("192.168.1.1"));
        var monitoredEntry = new ReportingApi.ListsResponseEntry(true, "monitored-key", "source", "blocked", List.of("192.168.2.*"));

        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
            List.of(blockedEntry, monitoredEntry),
            Collections.emptyList(),
            Collections.emptyList()
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.1");
        assertTrue(result.blocked());
        assertEquals("blocked", result.description());
        assertEquals(1, StatisticsStore.getStatsRecord().ipAddresses().get("blocked-key").total());
        assertEquals(1, StatisticsStore.getStatsRecord().ipAddresses().get("blocked-key").blocked());
        assertNull(StatisticsStore.getStatsRecord().ipAddresses().get("monitored-key"));

        // now test with multiple ip that are only monitored
        assertFalse(serviceConfiguration.isIpBlocked("192.168.2.20").blocked());
        assertFalse(serviceConfiguration.isIpBlocked("192.168.2.128").blocked());
        assertFalse(serviceConfiguration.isIpBlocked("192.168.3.20").blocked());
        assertFalse(serviceConfiguration.isIpBlocked("192.168.2.5").blocked());
        assertEquals(1, StatisticsStore.getStatsRecord().ipAddresses().get("blocked-key").total());
        assertEquals(1, StatisticsStore.getStatsRecord().ipAddresses().get("blocked-key").blocked());
        assertEquals(3, StatisticsStore.getStatsRecord().ipAddresses().get("monitored-key").total());
        assertEquals(0, StatisticsStore.getStatsRecord().ipAddresses().get("monitored-key").blocked());
    }

    @Test
    public void testIsIpAllowed() {
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "allowed", List.of("192.168.1.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                Collections.emptyList(),
                List.of(allowedEntry),
                Collections.emptyList()
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
                List.of(new ReportingApi.BotBlocklist(false, "blocked-bots", "blocked-agent"))
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertTrue(serviceConfiguration.isBlockedUserAgent("blocked-agent"));
        assertFalse(serviceConfiguration.isBlockedUserAgent("allowed-agent"));
        assertEquals(1, StatisticsStore.getStatsRecord().userAgents().get("blocked-bots").total());
        assertEquals(1, StatisticsStore.getStatsRecord().userAgents().get("blocked-bots").blocked());
    }

    @Test
    public void testMonitoredAndBlockedUserAgents() {
        var monitoring1 = new ReportingApi.BotBlocklist(true, "monitoring1", "blocked-agent");
        var blocking1 = new ReportingApi.BotBlocklist(false, "blocking1", "blocked-agent");
        var monitoring2 = new ReportingApi.BotBlocklist(true, "monitoring2", "allowed*");
        var blocking2 = new ReportingApi.BotBlocklist(false, "blocking2", "blocked*");

        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
            Collections.emptyList(),
            Collections.emptyList(),
            List.of(monitoring1, blocking1, monitoring2, blocking2)
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertTrue(serviceConfiguration.isBlockedUserAgent("blocked-agent"));
        assertTrue(serviceConfiguration.isBlockedUserAgent("blocked-a2"));
        assertFalse(serviceConfiguration.isBlockedUserAgent("allowed-agent"));
        assertFalse(serviceConfiguration.isBlockedUserAgent("allowed-a2"));
        assertFalse(serviceConfiguration.isBlockedUserAgent("allowed-a3"));
        assertEquals(1, StatisticsStore.getStatsRecord().userAgents().get("blocking1").total());
        assertEquals(1, StatisticsStore.getStatsRecord().userAgents().get("blocking1").blocked());
        assertEquals(2, StatisticsStore.getStatsRecord().userAgents().get("blocking2").total());
        assertEquals(2, StatisticsStore.getStatsRecord().userAgents().get("blocking2").blocked());
        assertEquals(1, StatisticsStore.getStatsRecord().userAgents().get("monitoring1").total());
        assertEquals(0, StatisticsStore.getStatsRecord().userAgents().get("monitoring1").blocked());
        assertEquals(3, StatisticsStore.getStatsRecord().userAgents().get("monitoring2").total());
        assertEquals(0, StatisticsStore.getStatsRecord().userAgents().get("monitoring2").blocked());
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
                Collections.emptyList()
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.1");
        assertFalse(result.blocked());
    }

    @Test
    public void testIsIpBlockedWithMultipleBlockedEntries() {
        ReportingApi.ListsResponseEntry blockedEntry1 = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("192.168.1.1"));
        ReportingApi.ListsResponseEntry blockedEntry2 = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("192.168.1.2"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry1, blockedEntry2),
                Collections.emptyList(),
                Collections.emptyList()
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result1 = serviceConfiguration.isIpBlocked("192.168.1.1");
        ServiceConfiguration.BlockedResult result2 = serviceConfiguration.isIpBlocked("192.168.1.2");
        assertTrue(result1.blocked());
        assertTrue(result2.blocked());
    }

    @Test
    public void testIsIpBlockedWithMixedAllowedAndBlockedEntries() {
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "allowed", List.of("192.168.1.1"));
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("192.168.1.2"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                List.of(allowedEntry),
                Collections.emptyList()
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
                Collections.emptyList()
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertFalse(serviceConfiguration.isBlockedUserAgent("any-agent"));
    }

    @Test
    public void testIsBlockedUserAgentWithCaseInsensitiveMatch() {
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                Collections.emptyList(),
                Collections.emptyList(),
            List.of(new ReportingApi.BotBlocklist(false, "blocked-bots", "blocked-agent"))
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
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("192.168.1.0/24"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                Collections.emptyList(),
                Collections.emptyList()
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult result = serviceConfiguration.isIpBlocked("192.168.1.100");
        assertTrue(result.blocked());
        assertEquals("blocked", result.description());
    }

    @Test
    public void testIsIpBlockedWithMixedSubnetAndSingleIP() {
        ReportingApi.ListsResponseEntry blockedEntry1 = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("192.168.1.0/24"));
        ReportingApi.ListsResponseEntry blockedEntry2 = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("10.0.0.1"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry1, blockedEntry2),
                Collections.emptyList(),
                Collections.emptyList()
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
                Collections.emptyList()
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
                List.of()
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertFalse(serviceConfiguration.isBlockedUserAgent("any-agent"));
    }

    @Test
    public void testIsBlockedUserAgentWithComplexRegex() {
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(),
                List.of(),
                List.of(
                    new ReportingApi.BotBlocklist(false, "block1", "blocked.*agent"),
                    new ReportingApi.BotBlocklist(false, "block2", "another.*pattern")
                )
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        assertTrue(serviceConfiguration.isBlockedUserAgent("blocked-agent"));
        assertTrue(serviceConfiguration.isBlockedUserAgent("another-pattern"));
        assertFalse(serviceConfiguration.isBlockedUserAgent("allowed-agent"));
    }

    @Test
    public void testIsIpBlockedWithAllowedAndBlockedIPs() {
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "allowed", List.of("10.0.0.1"));
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("192.168.1.1"));
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
        ReportingApi.ListsResponseEntry allowedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "allowed", List.of("10.0.0.1"));
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
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("192.168.1.1"));
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
        ReportingApi.ListsResponseEntry allowedEntry1 = new ReportingApi.ListsResponseEntry(false, "key", "source", "allowed", List.of("10.0.0.1"));
        ReportingApi.ListsResponseEntry allowedEntry2 = new ReportingApi.ListsResponseEntry(false, "key", "source", "allowed", List.of("10.0.0.2"));
        ReportingApi.ListsResponseEntry allowedEntry3 = new ReportingApi.ListsResponseEntry(false, "key", "source", "allowed", List.of("10.0.0.3"));
        ReportingApi.ListsResponseEntry blockedEntry = new ReportingApi.ListsResponseEntry(false, "key", "source", "blocked", List.of("10.0.0.2"));
        ReportingApi.APIListsResponse listsResponse = new ReportingApi.APIListsResponse(
                List.of(blockedEntry),
                List.of(allowedEntry1, allowedEntry2, allowedEntry3),
                null
        );

        serviceConfiguration.updateBlockedLists(listsResponse);

        ServiceConfiguration.BlockedResult resultAllowed = serviceConfiguration.isIpBlocked("10.0.0.1");
        ServiceConfiguration.BlockedResult resultAllowed2 = serviceConfiguration.isIpBlocked("10.0.0.3");
        ServiceConfiguration.BlockedResult resultBlocked = serviceConfiguration.isIpBlocked("10.0.0.2");
        ServiceConfiguration.BlockedResult resultNotAllowedLocal = serviceConfiguration.isIpBlocked("192.168.1.2");
        assertFalse(resultAllowed.blocked());
        assertFalse(resultAllowed2.blocked());
        assertTrue(resultBlocked.blocked());
        assertFalse(resultNotAllowedLocal.blocked());
    }
}
