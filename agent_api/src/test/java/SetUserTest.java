import dev.aikido.agent_api.SetUser;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
            this.body = new HashMap<>();
            this.body.put("test", "{\"key\":\"value\"}"); // Body as a JSON string
            this.executedMiddleware = true; // Start with "executed middleware" as true
        }
    }

    @BeforeAll
    public static void clean() {
        Context.set(null);
        ThreadCache.set(null);
    };
    TestAppender testAppender;
    @BeforeEach
    public void setUp() throws SQLException {
        testAppender = new TestAppender("TestAppender");
        testAppender.start();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        config.addAppender(testAppender);
        LoggerConfig loggerConfig = config.getLoggerConfig(SetUser.class.getName());
        loggerConfig.addAppender(testAppender, org.apache.logging.log4j.Level.INFO, null);
        loggerConfig.setLevel(org.apache.logging.log4j.Level.INFO);
        context.updateLoggers();
        // Connect to the MySQL database
        ThreadCache.set(new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes()));
    }

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        ThreadCache.set(null);
        testAppender.clear();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        // Remove the appender from the logger configuration
        LoggerConfig loggerConfig = config.getLoggerConfig(SetUser.class.getName());
        loggerConfig.removeAppender(testAppender.getName());

        // Update the logger context
        context.updateLoggers();
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testIndependenceFromThreadCache() throws SQLException {
        Context.set(new SampleContextObject());
        // Test with thread cache set :
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertTrue(testAppender.getLogMessages().contains("setUser(...) must be called before the Zen middleware is executed."));

        testAppender.clear();

        // Test with thread cache set to null:
        ThreadCache.set(null);
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertTrue(testAppender.getLogMessages().contains("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testValidAndInvalidUsers() throws SQLException {
        Context.set(new SampleContextObject());
        // Test with invalid user 1 :
        SetUser.setUser(new SetUser.UserObject("", "Name"));
        assertTrue(testAppender.getLogMessages().contains("User ID or name cannot be empty."));

        // Test with invalid user 2 :
        testAppender.clear();
        SetUser.setUser(new SetUser.UserObject("ID", ""));
        assertTrue(testAppender.getLogMessages().contains("User ID or name cannot be empty."));

        // Test with invalid user 3 :
        testAppender.clear();
        SetUser.setUser(new SetUser.UserObject("", ""));
        assertTrue(testAppender.getLogMessages().contains("User ID or name cannot be empty."));

        // Test with invalid user 4 :
        testAppender.clear();
        SetUser.setUser(new SetUser.UserObject(null, ""));
        assertTrue(testAppender.getLogMessages().contains("User ID or name cannot be empty."));

        // Test with invalid user 5 :
        testAppender.clear();
        SetUser.setUser(new SetUser.UserObject(null, null));
        assertTrue(testAppender.getLogMessages().contains("User ID or name cannot be empty."));

        // Test with invalid user 6 :
        testAppender.clear();
        SetUser.setUser(new SetUser.UserObject("ID", null));
        assertTrue(testAppender.getLogMessages().contains("User ID or name cannot be empty."));
        // Test with valid user :
        testAppender.clear();
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertTrue(testAppender.getLogMessages().contains("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testWithVaryingCOntext() throws SQLException {
        // Test with context not set :
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(0, testAppender.getLogMessages().size());

        // Test with context not set and user invalid :
        testAppender.clear();
        SetUser.setUser(new SetUser.UserObject("ID", null));
        assertTrue(testAppender.getLogMessages().contains("User ID or name cannot be empty."));

        // Test with context set but executed middleware false:
        ContextObject ctx = new SampleContextObject();
        ctx.setExecutedMiddleware(false);
        Context.set(ctx);

        testAppender.clear();
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(0, testAppender.getLogMessages().size());
    }

}
