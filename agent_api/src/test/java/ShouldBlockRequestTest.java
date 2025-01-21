import dev.aikido.agent_api.SetUser;
import dev.aikido.agent_api.ShouldBlockRequest;
import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

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
        ThreadCache.set(null);
    };
    @BeforeEach
    public void setUp() throws SQLException {
        // Connect to the MySQL database
        ThreadCache.set(getEmptyThreadCacheObject());
    }

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        ThreadCache.set(null);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testNoThreadCache() throws SQLException {
        Context.set(new SampleContextObject());
        // Test with thread cache set :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertTrue(Context.get().middlewareExecuted());
        assertFalse(res1.block());


        Context.set(new SampleContextObject());
        ThreadCache.set(null);
        // Test with thread cache not set :
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(Context.get().middlewareExecuted());
        assertFalse(res2.block());

        Context.reset();
        // Test with context not set :
        var res3 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(Context.get().middlewareExecuted());
        assertFalse(ThreadCache.get().isMiddlewareInstalled());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testNoContext() throws SQLException {
        Context.set(null);
        // Test with thread cache set :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());

        Context.set(null);
        ThreadCache.set(null);
        // Test with thread cache not set :
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res2.block());
    }


    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testUserSet() throws SQLException {
        ContextObject ctx = new SampleContextObject();

        ctx.setUser(new User("ID1", "John Doe", "127.0.0.1", 100));
        Context.set(ctx);
        // Test with user set but not blocked :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());
        assertTrue(Context.get().middlewareExecuted());
        assertTrue(ThreadCache.get().isMiddlewareInstalled());

        // Test with user set and blocked :
        ctx = new SampleContextObject();
        ctx.setUser(new User("ID1", "John Doe", "127.0.0.1", 100));
        Context.set(ctx);

        ThreadCache.set(new ThreadCacheObject(List.of(), Set.of("ID1", "ID2", "ID3"), Set.of(), new Routes(), Optional.empty()));
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertTrue(res2.block());
        assertTrue(Context.get().middlewareExecuted());
        assertTrue(ThreadCache.get().isMiddlewareInstalled());
        assertEquals("user", res2.data().trigger());
        assertEquals("blocked", res2.data().type());
        assertEquals("192.168.1.1", res2.data().ip());



        // Test users blocked but no user set :
        Context.set(new SampleContextObject());
        var res3 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res3.block());
        assertTrue(Context.get().middlewareExecuted());
        assertTrue(ThreadCache.get().isMiddlewareInstalled());

        // Test users blocked but user not blocked :
        ctx = new SampleContextObject();
        ctx.setUser(new User("ID4", "John Doe", "127.0.0.1", 100));
        Context.set(ctx);
        var res4 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res4.block());
        assertTrue(Context.get().middlewareExecuted());
        assertTrue(ThreadCache.get().isMiddlewareInstalled());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testEndpointsExistButNoMatch() throws SQLException {
        Context.set(null);
        ThreadCache.set(new ThreadCacheObject(List.of(
                new Endpoint("POST", "/api2/*", 1, 1000, Collections.emptyList(), false, false, false)
        ), Set.of(), Set.of(), new Routes(), Optional.empty()));

        // Test with thread cache set & rate-limiting disabled :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());

        Context.set(null);
        ThreadCache.set(new ThreadCacheObject(List.of(
                new Endpoint("POST", "/api2/*", 1, 1000, Collections.emptyList(), false, false, true)
        ), Set.of(), Set.of(), new Routes(), Optional.empty()));

        // Test with thread cache set & rate-limiting enabled :
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res2.block());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testEndpointsExistWithMatch() throws SQLException {
        Context.set(null);
        ThreadCache.set(new ThreadCacheObject(List.of(
                new Endpoint("GET", "/api/*", 1, 1000, Collections.emptyList(), false, false, false)
        ), Set.of(), Set.of(), new Routes(), Optional.empty()));

        // Test with match & rate-limiting disabled :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());

        Context.set(null);
        ThreadCache.set(new ThreadCacheObject(List.of(
                new Endpoint("GET", "/api/*", 1, 1000, Collections.emptyList(), false, false, true)
        ), Set.of(), Set.of(), new Routes(), Optional.empty()));

        // Test with match & rate-limiting enabled :
        var res2 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res2.block());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "")
    public void testThreadClientInvalid() throws SQLException {
        Context.set(new SampleContextObject());
        ThreadCache.set(new ThreadCacheObject(List.of(
                new Endpoint("GET", "/api/*", 1, 1000, Collections.emptyList(), false, false, true)
        ), Set.of(), Set.of(), new Routes(), Optional.empty()));

        // Test with match & rate-limiting enabled :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());
    }

    @Test
    @ClearEnvironmentVariable(key = "AIKIDO_TOKEN")
    public void testThreadClientInvalid2() throws SQLException {
        Context.set(new SampleContextObject());
        ThreadCache.set(new ThreadCacheObject(List.of(
                new Endpoint("GET", "/api/*", 1, 1000, Collections.emptyList(), false, false, true)
        ), Set.of(), Set.of(), new Routes(), Optional.empty()));

        // Test with match & rate-limiting enabled :
        var res1 = ShouldBlockRequest.shouldBlockRequest();
        assertFalse(res1.block());
    }

}
