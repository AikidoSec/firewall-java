package thread_cache;

import dev.aikido.agent_api.background.BackgroundProcess;
import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.ipc_commands.InitRouteCommand;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import dev.aikido.agent_api.thread_cache.ThreadCacheRenewal;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;
import static org.junit.jupiter.api.Assertions.*;

public class ThreadCacheRenewalTest {
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "")
    public void renewWithoutValidToken() {
        assertNull(ThreadCacheRenewal.renewThreadCache());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "token-123456")
    // Set AIKIDO_TMP_DIR to /opt/aikido, this is what gets created for Github workflows
    @SetEnvironmentVariable(key = "AIKIDO_TMP_DIR", value = "/opt/aikido")
    public void renewWithEmptyBackgroundProcess() throws InterruptedException {
        BackgroundProcess backgroundProcess = new BackgroundProcess("test-background-process", new Token("token-123456"));
        backgroundProcess.setDaemon(true);
        backgroundProcess.start();
        Thread.sleep(8*1000); // Wait for bg process to initialize

        ThreadCacheObject threadCacheObject = ThreadCacheRenewal.renewThreadCache();
        assertNotNull(threadCacheObject);
        assertEquals(0, threadCacheObject.getEndpoints().size());
        assertEquals(0, threadCacheObject.getRoutes().size());

        // Now report a route :
        RouteMetadata routeMetadata = new RouteMetadata("/myroute", "http://localhost:8080/myroute", "POST");
        new InitRouteCommand().send(getDefaultThreadIPCClient(), routeMetadata);

        // Test :
        threadCacheObject = ThreadCacheRenewal.renewThreadCache();
        assertNotNull(threadCacheObject);
        assertEquals(1, threadCacheObject.getRoutes().size()); // Changed!
        assertNotNull(threadCacheObject.getRoutes().get(routeMetadata));
        backgroundProcess.interrupt();
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "token-1234567")
    // Set AIKIDO_TMP_DIR to /opt/aikido, this is what gets created for Github workflows
    @SetEnvironmentVariable(key = "AIKIDO_TMP_DIR", value = "/opt/aikido")
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "http://localhost:5000")
    public void renewWithLinkedUpBackgroundProcess() throws InterruptedException {
        BackgroundProcess backgroundProcess = new BackgroundProcess("test-background-process", new Token("token-1234567"));
        backgroundProcess.setDaemon(true);
        backgroundProcess.start();
        Thread.sleep(5*1000); // Wait for bg process to initialize

        ThreadCacheObject threadCacheObject = ThreadCacheRenewal.renewThreadCache();
        assertNotNull(threadCacheObject);

        // Test the endpoints :
        assertEquals(1, threadCacheObject.getEndpoints().size());
        Endpoint endpoint1 = threadCacheObject.getEndpoints().get(0);
        assertEquals("*", endpoint1.getMethod());
        assertEquals("/test_ratelimiting_1", endpoint1.getRoute());
        assertEquals(2, endpoint1.getRateLimiting().maxRequests());
        assertEquals(0, threadCacheObject.getRoutes().size());

        // Test the blocked IP lists :
        assertTrue(threadCacheObject.isIpBlocked("1.2.3.4").blocked());
        assertEquals("geo restrictions", threadCacheObject.isIpBlocked("1.2.3.4").description());

        assertFalse(threadCacheObject.isIpBlocked("5.6.7.8").blocked());
        assertNull(threadCacheObject.isIpBlocked("5.6.7.8").description());

        backgroundProcess.interrupt();
    }

}
