import dev.aikido.agent_api.ShouldBlockRequest;
import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static org.junit.jupiter.api.Assertions.*;
import static utils.EmptyAPIResponses.emptyAPIResponse;
import static utils.EmptyAPIResponses.setEmptyConfigWithEndpointList;


public class ShouldBlockRequestTest {
    public static class SampleContextObject extends ContextObject {
        public SampleContextObject() {
            this.method = "GET";
            this.source = "web";
            this.url = "https://example.com/api/resource";
            this.route = "/api/resource";
            this.remoteAddress = "192.168.1.1";
            this.headers = new HashMap<>();
            this.query = new HashMap<>();
            this.cookies = new HashMap<>();
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
            this.executedMiddleware = false; // Start with "executed middleware" as false
        }
    }

    @BeforeAll
    public static void clean() {
        Context.set(null);
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
    };

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
    }

    @Test
    public void testNoContext() throws SQLException {
        Context.set(null);
        // Test with thread cache set :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());

        Context.set(null);
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);

        // Test with thread cache not set :
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res2.block());
    }

    @Test
    public void testUserSet() throws SQLException {
        ContextObject ctx = new SampleContextObject();

        ctx.setUser(new User("ID1", "John Doe", "127.0.0.1", 100));
        Context.set(ctx);
        // Test with user set but not blocked :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());
        assertTrue(Context.get().middlewareExecuted());
        assertTrue(ServiceConfigStore.getConfig().isMiddlewareInstalled());

        // Test with user set and blocked :
        ctx = new SampleContextObject();
        ctx.setUser(new User("ID1", "John Doe", "127.0.0.1", 100));
        Context.set(ctx);

        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, "", getUnixTimeMS(), List.of(),
                /* blockedUserIds */ List.of("ID1", "ID2", "ID3"), List.of(),
                false, true
        ));
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertTrue(res2.block());
        assertTrue(Context.get().middlewareExecuted());
        assertTrue(ServiceConfigStore.getConfig().isMiddlewareInstalled());
        assertEquals("user", res2.data().trigger());
        assertEquals("blocked", res2.data().type());
        assertEquals("192.168.1.1", res2.data().ip());



        // Test users blocked but no user set :
        Context.set(new SampleContextObject());
        var res3 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res3.block());
        assertTrue(Context.get().middlewareExecuted());
        assertTrue(ServiceConfigStore.getConfig().isMiddlewareInstalled());

        // Test users blocked but user not blocked :
        ctx = new SampleContextObject();
        ctx.setUser(new User("ID4", "John Doe", "127.0.0.1", 100));
        Context.set(ctx);
        var res4 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res4.block());
        assertTrue(Context.get().middlewareExecuted());
        assertTrue(ServiceConfigStore.getConfig().isMiddlewareInstalled());
    }

    @Test
    public void testEndpointsExistButNoMatch() throws SQLException {
        Context.set(null);
        setEmptyConfigWithEndpointList(List.of(
                new Endpoint("POST", "/api2/*", 1, 1000, Collections.emptyList(), false, false, false)
        ));

        // Test with thread cache set & rate-limiting disabled :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());

        Context.set(null);
        setEmptyConfigWithEndpointList(List.of(
                new Endpoint("POST", "/api2/*", 1, 1000, Collections.emptyList(), false, false, true)
        ));

        // Test with thread cache set & rate-limiting enabled :
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res2.block());
    }

    @Test
    public void testEndpointsExistWithMatch() throws SQLException {
        Context.set(null);
        setEmptyConfigWithEndpointList(List.of(
                new Endpoint("GET", "/api/*", 1, 1000, Collections.emptyList(), false, false, false)
        ));

        // Test with match & rate-limiting disabled :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());

        Context.set(null);
        setEmptyConfigWithEndpointList(List.of(
                new Endpoint("GET", "/api/*", 1, 1000, Collections.emptyList(), false, false, true)
        ));

        // Test with match & rate-limiting enabled :
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res2.block());
    }

    @Test
    public void testThreadClientInvalid() throws SQLException {
        Context.set(new SampleContextObject());
        setEmptyConfigWithEndpointList(List.of(
                new Endpoint("GET", "/api/*", 1, 1000, Collections.emptyList(), false, false, true)
        ));

        // Test with match & rate-limiting enabled :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());
    }

    @Test
    public void testNoEndpointsConfigured() throws SQLException {
        // Set up context with a user
        ContextObject ctx = new SampleContextObject();
        ctx.setUser(new User("ID3", "Alice", "192.168.1.3", 100));
        Context.set(ctx);

        // Set up thread cache with no endpoints
        setEmptyConfigWithEndpointList(List.of());

        // Call the method
        var res = ShouldBlockRequest.shouldBlockRequest();

        // Assert that the request is not blocked
        assertFalse(res.block());
    }

    @Test
    public void testBlockedUserWithMultipleEndpoints() throws SQLException {
        // Set up context with a blocked user
        ContextObject ctx = new SampleContextObject();
        ctx.setUser(new User("ID1", "John Doe", "192.168.1.1", 100));
        Context.set(ctx);

        // Set up thread cache with multiple endpoints and a blocked user
        List<Endpoint> endpoints = List.of(
                new Endpoint("GET", "/api/resource", 1, 1000, Collections.emptyList(), false, false, true),
                new Endpoint("POST", "/api/resource", 1, 1000, Collections.emptyList(), false, false, true)
        );
        List<String> blockedUserIds = List.of("ID1");
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, "", getUnixTimeMS(), endpoints, blockedUserIds, List.of(), true, false
        ));

        // Call the method
        var res = ShouldBlockRequest.shouldBlockRequest();

        // Assert that the request is blocked due to the user being blocked
        assertTrue(res.block());
        assertEquals("user", res.data().trigger());
        assertEquals("blocked", res.data().type());
        assertEquals("192.168.1.1", res.data().ip());
    }

    @Test
    public void testNoUserWithEndpoints() throws SQLException {
        // Set up context without a user
        Context.set(new SampleContextObject());

        // Set up thread cache with endpoints
        setEmptyConfigWithEndpointList(List.of(
                new Endpoint("GET", "/api/resource", 1, 1000, Collections.emptyList(), false, false, true)
        ));

        // Call the method
        var res = ShouldBlockRequest.shouldBlockRequest();

        // Assert that the request is not blocked
        assertFalse(res.block());
    }
}
