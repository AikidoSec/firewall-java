package thread_cache;

import dev.aikido.agent_api.thread_cache.ThreadCacheRenewal;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ThreadCacheRenewalTest {
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "")
    public void renewWithoutValidToken() {
        assertNull(ThreadCacheRenewal.renewThreadCache());
    }
}
