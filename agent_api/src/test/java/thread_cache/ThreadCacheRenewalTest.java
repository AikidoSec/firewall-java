package thread_cache;

import dev.aikido.agent_api.background.BackgroundProcess;
import dev.aikido.agent_api.background.ipc_commands.InitRouteCommand;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import dev.aikido.agent_api.thread_cache.ThreadCacheRenewal;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;
import static org.junit.jupiter.api.Assertions.*;

public class ThreadCacheRenewalTest {
    /*
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "")
    public void renewWithoutValidToken() {
        assertNull(ThreadCacheRenewal.renewThreadCache());
    }*/

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "token-123456")
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

}
