import dev.aikido.agent_api.SetRateLimitGroup;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SetEnvironmentVariable(key = "AIKIDO_LOG_LEVEL", value = "trace")
@SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
public class SetRateLimitGroupTest {
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
    }

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
    }

    @Test
    @StdIo
    public void testStartWithExecutedMiddlewareAsTrue(StdOut out) throws SQLException, IOException {
        Context.set(new SampleContextObject());
        SetRateLimitGroup.setRateLimitGroup("id");
        assertTrue(out.capturedString().contains("setRateLimitGroup(...) must be called before the Zen middleware is executed."));
    }

    @Test
    @StdIo
    public void testWithNoContext(StdOut out) throws SQLException, IOException {
        SetRateLimitGroup.setRateLimitGroup("id");
        assertFalse(out.capturedString().contains("setRateLimitGroup(...) must be called before the Zen middleware is executed."));
        assertTrue(out.capturedString().contains("setRateLimitGroup(...) was called without a context. Make sure to call setRateLimitGroup(...) within an HTTP request."));
    }

    @Test
    @StdIo
    public void testEmptyGroupId(StdOut out) throws SQLException {
        Context.set(new SampleContextObject());

        SetRateLimitGroup.setRateLimitGroup("");

        assertTrue(out.capturedString().contains("Group ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testNullGroupId(StdOut out) throws SQLException {
        Context.set(new SampleContextObject());

        SetRateLimitGroup.setRateLimitGroup(null);

        assertTrue(out.capturedString().contains("Group ID or name cannot be empty."));
    }

    @Test
    @StdIo
    public void testValid(StdOut out) throws SQLException {
        // Test with context set but executed middleware false:
        ContextObject ctx = new SampleContextObject();
        ctx.setExecutedMiddleware(false);
        Context.set(ctx);

        SetRateLimitGroup.setRateLimitGroup("group-id");

        // assert no logs :
        assertFalse(out.capturedString().contains("SetRateLimitGroup"));

        assertEquals("group-id", ctx.getRateLimitGroup());
    }
}
