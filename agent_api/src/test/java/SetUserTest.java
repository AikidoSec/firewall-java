import dev.aikido.agent_api.SetUser;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import dev.aikido.agent_api.vulnerabilities.sql_injection.SQLInjectionException;
import nl.altindag.log.LogCaptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.ArgumentCaptor;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

public class SetUserTest {
    private Logger logger;
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
        logger = LogManager.getLogger(SetUser.class);
        // Connect to the MySQL database
        ThreadCache.set(new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes()));
    }

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        ThreadCache.set(null);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testIndependenceFromThreadCache() throws SQLException {
        Context.set(new SampleContextObject());
        // Test with thread cache set :
        var logCaptor = LogCaptor.forClass(SetUser.class);
        logCaptor.setLogLevelToInfo();
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertTrue(logCaptor.getInfoLogs().contains("setUser(...) must be called before the Zen middleware is executed."));

        // Test with thread cache set to null:
        ThreadCache.set(null);
        logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertTrue(logCaptor.getInfoLogs().contains("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testValidAndInvalidUsers() throws SQLException {
        Context.set(new SampleContextObject());
        // Test with invalid user 1 :
        var logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject("", "Name"));
        assertTrue(logCaptor.getInfoLogs().contains("User ID or name cannot be empty."));

        // Test with invalid user 2 :
        logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject("ID", ""));
        assertTrue(logCaptor.getInfoLogs().contains("User ID or name cannot be empty."));

        // Test with invalid user 3 :
        logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject("", ""));
        assertTrue(logCaptor.getInfoLogs().contains("User ID or name cannot be empty."));

        // Test with invalid user 4 :
        logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject(null, ""));
        assertTrue(logCaptor.getInfoLogs().contains("User ID or name cannot be empty."));

        // Test with invalid user 5 :
        logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject(null, null));
        assertTrue(logCaptor.getInfoLogs().contains("User ID or name cannot be empty."));

        // Test with invalid user 6 :
        logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject("ID", null));
        assertTrue(logCaptor.getInfoLogs().contains("User ID or name cannot be empty."));
        // Test with valid user :
        logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertTrue(logCaptor.getInfoLogs().contains("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testWithVaryingCOntext() throws SQLException {
        // Test with context not set:
        var logCaptor = LogCaptor.forClass(SetUser.class);
        logCaptor.setLogLevelToInfo();
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(0, logCaptor.getInfoLogs().size());

        // Test with context not set and user invalid :
        logCaptor = LogCaptor.forClass(SetUser.class);
        SetUser.setUser(new SetUser.UserObject("ID", null));
        assertTrue(logCaptor.getInfoLogs().contains("User ID or name cannot be empty."));

        // Test with context set but executed middleware false:
        ContextObject ctx = new SampleContextObject();
        ctx.setExecutedMiddleware(false);
        Context.set(ctx);

        logCaptor = LogCaptor.forClass(SetUser.class);
        logCaptor.setLogLevelToInfo();
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(0, logCaptor.getInfoLogs().size());
    }
}
