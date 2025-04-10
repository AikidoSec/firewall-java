package background;

import dev.aikido.agent_api.background.BackgroundProcess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BackgroundProcessTest {
    @Test
    void testRunWithNullToken() throws InterruptedException {
        // Create a BackgroundProcess with a null token
        BackgroundProcess processWithNullToken = new BackgroundProcess("TestThread", null);
        processWithNullToken.start();
        Thread.sleep(100); // Wait for a short time to allow the thread to start

        // The thread should not run if the token is null
        assertFalse(processWithNullToken.isAlive());
    }
}