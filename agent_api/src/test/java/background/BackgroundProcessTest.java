package background;


import dev.aikido.agent_api.background.BackgroundProcess;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.helpers.env.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackgroundProcessTest {

    private BackgroundProcess backgroundProcess;

    @BeforeEach
    void setUp() {
        backgroundProcess = new BackgroundProcess("TestThread", new Token("token"));
    }

    @Test
    void testRunWithNullToken() throws InterruptedException {
        // Create a BackgroundProcess with a null token
        BackgroundProcess processWithNullToken = new BackgroundProcess("TestThread", null);
        processWithNullToken.start();
        Thread.sleep(100); // Wait for a short time to allow the thread to start

        // The thread should not run if the token is null
        assertFalse(processWithNullToken.isAlive());
    }

    @Test
    void testRunWithValidToken() throws InterruptedException {
        // Start the background process
        backgroundProcess.setDaemon(true);
        backgroundProcess.start();

        Thread.sleep(100); // Wait for a short time to allow the thread to start

        // Check that the thread is alive
        assertTrue(backgroundProcess.isAlive());

        // Check that the connection manager is initialized
        CloudConnectionManager connectionManager = backgroundProcess.getCloudConnectionManager();
        assertNotNull(connectionManager);

        // Check that the attack queue is initialized
        BlockingQueue<APIEvent> attackQueue = backgroundProcess.getAttackQueue();
        assertNotNull(attackQueue);
    }

    @Test
    void testAttackQueueInitialization() throws InterruptedException {
        // Start the background process
        backgroundProcess.setDaemon(true);
        backgroundProcess.start();
        Thread.sleep(100); // Wait for a short time to allow the thread to start

        // Check that the attack queue is initialized
        BlockingQueue<APIEvent> attackQueue = backgroundProcess.getAttackQueue();
        assertNotNull(attackQueue);
        assertTrue(attackQueue.isEmpty());
    }
}