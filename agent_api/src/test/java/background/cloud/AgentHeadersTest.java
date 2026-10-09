package background.cloud;

import static org.junit.jupiter.api.Assertions.*;

import dev.aikido.agent_api.background.cloud.AgentHeaders;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

public class AgentHeadersTest {
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_INSTANCE_NAME", value = "my-instance")
    public void testUsesInstanceNameAndSameSessionIdOnEveryCall() {
        assertEquals("my-instance", AgentHeaders.get().get("X-Agent-Hostname"));

        String sessionId = AgentHeaders.get().get("X-Agent-Session-Id");
        assertEquals(7, UUID.fromString(sessionId).version());
        assertEquals(sessionId, AgentHeaders.get().get("X-Agent-Session-Id"));
    }
}
