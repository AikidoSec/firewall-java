import dev.aikido.agent_api.SetUser;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.junit.jupiter.api.*;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

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
    };
    ByteArrayOutputStream outputStream;
    PrintStream originalOut;
    @BeforeEach
    public void setUp() throws SQLException {
        // Connect to the MySQL database
        ThreadCache.set(getEmptyThreadCacheObject());
        originalOut = System.out;

        // Create a ByteArrayOutputStream to capture the output
        outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        // Redirect System.out to the new PrintStream
        System.setOut(printStream);

    }

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        ThreadCache.set(null);
        System.setOut(originalOut);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testIndependenceFromThreadCache() throws SQLException {
        Context.set(new SampleContextObject());
        // Test with thread cache set :
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(outputStream.toString(), ("setUser(...) must be called before the Zen middleware is executed."));

        outputStream.reset();

        // Test with thread cache set to null:
        ThreadCache.set(null);
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(outputStream.toString(), ("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testValidAndInvalidUsers() throws SQLException {
        Context.set(new SampleContextObject());
        // Test with invalid user 1 :
        SetUser.setUser(new SetUser.UserObject("", "Name"));
        assertTrue(outputStream.toString().contains("User ID or name cannot be empty."));

        // Test with invalid user 2 :
        outputStream.reset();
        SetUser.setUser(new SetUser.UserObject("ID", ""));
        assertTrue(outputStream.toString().contains("User ID or name cannot be empty."));

        // Test with invalid user 3 :
        outputStream.reset();
        SetUser.setUser(new SetUser.UserObject("", ""));
        assertTrue(outputStream.toString().contains("User ID or name cannot be empty."));

        // Test with invalid user 4 :
        outputStream.reset();
        SetUser.setUser(new SetUser.UserObject(null, ""));
        assertTrue(outputStream.toString().contains("User ID or name cannot be empty."));

        // Test with invalid user 5 :
        outputStream.reset();
        SetUser.setUser(new SetUser.UserObject(null, null));
        assertTrue(outputStream.toString().contains("User ID or name cannot be empty."));

        // Test with invalid user 6 :
        outputStream.reset();
        SetUser.setUser(new SetUser.UserObject("ID", null));
        assertTrue(outputStream.toString().contains("User ID or name cannot be empty."));
        // Test with valid user :
        outputStream.reset();
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertTrue(outputStream.toString().contains("setUser(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testWithVaryingCOntext() throws SQLException {
        // Test with context not set :
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(0, outputStream.toString().length());

        // Test with context not set and user invalid :
        outputStream.reset();
        SetUser.setUser(new SetUser.UserObject("ID", null));
        assertTrue(outputStream.toString().contains("User ID or name cannot be empty."));

        // Test with context set but executed middleware false:
        ContextObject ctx = new SampleContextObject();
        ctx.setExecutedMiddleware(false);
        Context.set(ctx);

        outputStream.reset();
        SetUser.setUser(new SetUser.UserObject("ID", "Name"));
        assertEquals(0, outputStream.toString().length());
    }
}
