package background;

import dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ThreadIPCClientFactoryTest {

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "")
    public void testWithEmptyString() {
        assertNull(ThreadIPCClientFactory.getDefaultThreadIPCClient());
    }

    @Test
    @ClearEnvironmentVariable(key = "AIKIDO_TOKEN")
    public void testWithNoAikidoToken() {
        assertNull(ThreadIPCClientFactory.getDefaultThreadIPCClient());
    }
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "not-empty-token")
    public void testWithNonEmptyToken() {
        assertNotNull(ThreadIPCClientFactory.getDefaultThreadIPCClient());
    }
}
