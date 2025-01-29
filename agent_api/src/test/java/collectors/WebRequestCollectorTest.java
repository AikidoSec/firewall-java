package collectors;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

class WebRequestCollectorTest {

    private ContextObject contextObject;
    private ThreadCacheObject threadCacheObject;

    @BeforeEach
    void setUp() {
        contextObject = new EmptySampleContextObject();
        contextObject.getHeaders().put("content-type", "application/json");
        contextObject.getHeaders().put("user-agent", "Mozilla/5.0 (compatible) AI2Bot (+https://www.allenai.org/crawler)");
        threadCacheObject = mock(ThreadCacheObject.class);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_contextSet() {
        // Mock ThreadCache
        threadCacheObject = getEmptyThreadCacheObject();
        ThreadCache.set(threadCacheObject);
        assertEquals(0, threadCacheObject.getTotalHits());

        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
        assertEquals(Context.get(), contextObject);
        assertEquals(1, threadCacheObject.getTotalHits());
        // Increment total hits with same context object :
        WebRequestCollector.report(contextObject);
        assertEquals(2, threadCacheObject.getTotalHits());

    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipNotAllowed() {
        // Mock ThreadCache
        threadCacheObject = new ThreadCacheObject(List.of(new Endpoint(
            "GET", "/api/resource", 100, 100,
            List.of("192.168.0.1"), false, false, false
        )), Set.of(), Set.of(), new Routes(), Optional.empty());
        ThreadCache.set(threadCacheObject);


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNotNull(response);
        assertEquals("Your IP address is not allowed to access this resource. (Your IP: 192.168.1.1)", response.msg());
        assertEquals(403, response.status());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_endpointAndIpAllowed() {
        // Mock ThreadCache
        threadCacheObject = new ThreadCacheObject(List.of(new Endpoint(
            "GET", "/api/resource", 100, 100,
            List.of("192.168.1.1"), false, false, false
        )), Set.of(), Set.of(), new Routes(), Optional.empty());
        ThreadCache.set(threadCacheObject);


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_noThreadCacheObject() {
        // Mock ThreadCache to return null
        ThreadCache.reset();

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
        ), "");
        // Mock ThreadCache
        threadCacheObject = new ThreadCacheObject(List.of(new Endpoint(
            "GET", "/api/resource", 100, 100,
            List.of("192.168.0.1"), false, false, false
        )), Set.of(), Set.of(), new Routes(), Optional.of(blockedListsRes));
        ThreadCache.set(threadCacheObject);


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
        ), "");
        // Mock ThreadCache
        threadCacheObject = new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes(), Optional.of(blockedListsRes));
        ThreadCache.set(threadCacheObject);


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNotNull(response);
        assertEquals("Your IP address is not allowed to access this resource. (Your IP: 192.168.1.1)", response.msg());
        assertEquals(403, response.status());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipNotBlockedUsingListsNorUserAgent() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(List.of(
            new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("192.168.1.2", "192.168.1.3"))
        ), "Unrelated|random");
        // Mock ThreadCache
        threadCacheObject = new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes(), Optional.of(blockedListsRes));
        ThreadCache.set(threadCacheObject);


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_userAgentBlocked() {
        ReportingApi.APIListsResponse blockedListsRes = new ReportingApi.APIListsResponse(List.of(
            new ReportingApi.ListsResponseEntry("geoip", "geoip restrictions", List.of("192.168.1.2", "192.168.1.3"))
        ), "AI2Bot|hacker");
        // Mock ThreadCache
        threadCacheObject = new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes(), Optional.of(blockedListsRes));
        ThreadCache.set(threadCacheObject);


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNotNull(response);
        assertEquals("You are not allowed to access this resource because you have been identified as a bot.", response.msg());
        assertEquals(403, response.status());
    }
}
