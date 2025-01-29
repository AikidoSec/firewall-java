import dev.aikido.agent_api.SetUser;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

@SetEnvironmentVariable(key = "AIKIDO_LOG_LEVEL", value = "trace")
@SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
public class SetUserTest {
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
            this.executedMiddleware = true; // Start with "executed middleware" as true
        }
    }

    @BeforeAll
    public static void clean() {
        Context.set(null);
        ThreadCache.set(null);
    }

    @BeforeEach
    public void setUp() throws SQLException {
        ThreadCache.set(getEmptyThreadCacheObject());
    }

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        ThreadCache.set(null);
    }

    @Test
    @StdIo
    public void testIndependenceFromThreadCacheSet(StdOut out) throws SQLException, IOException {
        Context.set(new SampleContextObject());
        // Test with thread cache set :
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertTrue(out.capturedString().contains("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @StdIo
    public void testIndependenceFromThreadCacheNull(StdOut out) throws SQLException, IOException {
        // Test with thread cache set to null:
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertFalse(out.capturedString().contains("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @StdIo
    public void testValidAndInvalidUsers1(StdOut out) throws SQLException {
        Context.set(new SampleContextObject());
        // Test with invalid user 1 :
        SetUser.setUser(new SetUser.UserObject("", "Name"));
        assertTrue(out.capturedString().contains("User ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testValidAndInvalidUsers2(StdOut out) throws SQLException {
        // Test with invalid user 2 :
        SetUser.setUser(new SetUser.UserObject("ID", ""));
        assertTrue(out.capturedString().contains("User ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testValidAndInvalidUsers3(StdOut out) throws SQLException {
        // Test with invalid user 3 :
        SetUser.setUser(new SetUser.UserObject("", ""));
        assertTrue(out.capturedString().contains("User ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testValidAndInvalidUsers4(StdOut out) throws SQLException {
        // Test with invalid user 4 :
        SetUser.setUser(new SetUser.UserObject(null, ""));
        assertTrue(out.capturedString().contains("User ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testValidAndInvalidUsers5(StdOut out) throws SQLException {
        // Test with invalid user 5 :
        SetUser.setUser(new SetUser.UserObject(null, null));
        assertTrue(out.capturedString().contains("User ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testValidAndInvalidUsers6(StdOut out) throws SQLException {
        // Test with invalid user 6 :
        SetUser.setUser(new SetUser.UserObject("ID", null));
        assertTrue(out.capturedString().contains("User ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testValidUser(StdOut out) throws SQLException {
        // Test with valid user :
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertFalse(out.capturedString().contains("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @StdIo
    public void testWithContextNotSet(StdOut out) throws SQLException {
        // Test with context not set :
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(0, out.capturedString().length());

        // Test with context not set and user invalid :
        SetUser.setUser(new SetUser.UserObject("ID", null));
        assertTrue(out.capturedString().contains("User ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testWithContextSetButNotExecutedMiddleware(StdOut out) throws SQLException {

        // Test with context set but executed middleware false:
        ContextObject ctx = new SampleContextObject();
        ctx.setExecutedMiddleware(false);
        Context.set(ctx);

        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertFalse(out.capturedString().contains("SetUser")); // Should not contain SetUser class
    }
}
