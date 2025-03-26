package collectors;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.ConfigStore;
import dev.aikido.agent_api.storage.StatisticsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptyAPIResponses;
import utils.EmptySampleContextObject;

import java.util.List;
import java.util.Optional;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static org.junit.jupiter.api.Assertions.*;
import static utils.EmptyAPIResponses.setEmptyConfigWithEndpointList;

class WebRequestCollectorTest {

    private EmptySampleContextObject contextObject;

    @BeforeEach
    void setUp() {
        contextObject = new EmptySampleContextObject();
        contextObject.getHeaders().put("content-type", List.of("application/json"));
        contextObject.getHeaders().put("user-agent", List.of("Mozilla/5.0 (compatible) AI2Bot (+https://www.allenai.org/crawler)"));
        StatisticsStore.clear();
        ConfigStore.updateFromAPIResponse(EmptyAPIResponses.emptyAPIResponse);
        ConfigStore.updateFromAPIListsResponse(Optional.of(EmptyAPIResponses.emptyAPIListsResponse));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_contextSet() {

        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
        assertEquals(Context.get(), contextObject);
        assertEquals(1, StatisticsStore.getStatsRecord().requests().total());
        // Increment total hits with same context object :
        WebRequestCollector.report(contextObject);
        assertEquals(2, StatisticsStore.getStatsRecord().requests().total());

    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipNotAllowed() {
        setEmptyConfigWithEndpointList(List.of(
                new Endpoint(
                        "GET", "/api/resource", 100, 100,
                        List.of("192.168.0.1"), false, false, false
                )
        ));


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNotNull(response);
        assertEquals("Your IP address is not allowed to access this resource. (Your IP: 192.168.1.1)", response.msg());
        assertEquals(403, response.status());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_endpointAndIpAllowed() {
        setEmptyConfigWithEndpointList(List.of(
                new Endpoint(
                        "GET", "/api/resource", 100, 100,
                        List.of("192.168.1.1"), false, false, false
                )
        ));

        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);
        assertNull(response);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_noThreadCacheObject() {
        // Act
        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        // Assert
        assertNull(response);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipBlockedTwice() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("192.168.1.1"))
        ), null, "");
        setEmptyConfigWithEndpointList(List.of(
            new Endpoint(
                "GET", "/api/resource", 100, 100,
                List.of("192.168.0.1"), false, false, false
            )
        ));
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNotNull(response);
        assertEquals("Your IP address is not allowed to access this resource. (Your IP: 192.168.1.1)", response.msg());
        assertEquals(403, response.status());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipBlockedUsingLists() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("bullshit.ip", "192.168.1.1"))
        ), null, "");
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNotNull(response);
        assertEquals("Your IP address is not allowed to access this resource. (Your IP: 192.168.1.1)", response.msg());
        assertEquals(403, response.status());
    }
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipNotAllowedUsingLists() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(null, List.of(
                new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("192.168.2.1"))
        ), "");
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response); // Private IP

        contextObject.setIp("4.4.4.4");
        response = WebRequestCollector.report(contextObject);
        assertNotNull(response);
        assertEquals("Your IP address is not allowed to access this resource. (Your IP: 4.4.4.4)", response.msg());
        assertEquals(403, response.status());
    }
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipInAllowlist() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(null, List.of(
                new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("192.168.1.1", "10.0.0.0/24"))
        ), "");
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipNotBlockedUsingListsNorUserAgent() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("192.168.1.2", "192.168.1.3"))
        ), null, "Unrelated|random");
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_userAgentBlocked() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("192.168.1.2", "192.168.1.3"))
        ), null, "AI2Bot|hacker");
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));

        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNotNull(response);
        assertEquals("You are not allowed to access this resource because you have been identified as a bot.", response.msg());
        assertEquals(403, response.status());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_userAgentBlocked_Ip_Bypassed() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("192.168.1.2", "192.168.1.3"))
        ), null, "AI2Bot|hacker");
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));

        List<String> bypassedIps = List.of("192.168.1.1");
        ConfigStore.updateFromAPIResponse(new APIResponse(
                true, "", getUnixTimeMS(), List.of(), List.of(), bypassedIps, true, false
        ));


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
        assertNull(Context.get());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipBlockedUsingLists_Ip_Bypassed() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("bullshit.ip", "192.168.1.1"))
        ), null, "");
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));

        List<String> bypassedIps = List.of("192.168.1.1");
        ConfigStore.updateFromAPIResponse(new APIResponse(
                true, "", getUnixTimeMS(), List.of(), List.of(), bypassedIps, true, false
        ));

        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
        assertNull(Context.get());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipNotAllowedUsingLists_Ip_Bypassed() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(
            null,
            List.of(new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("1.2.3.4"))),
            ""
        );
        ConfigStore.updateFromAPIListsResponse(Optional.of(blockedListsRes));

        List<String> bypassedIps = List.of("192.168.1.1");
        ConfigStore.updateFromAPIResponse(new APIResponse(
                true, "", getUnixTimeMS(), List.of(), List.of(), bypassedIps, true, false
        ));


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
        assertNull(Context.get());
    }
}
