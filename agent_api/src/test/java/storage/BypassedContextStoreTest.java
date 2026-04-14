package storage;

import dev.aikido.agent_api.storage.BypassedContextStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class BypassedContextStoreTest {

    @BeforeEach
    public void setUp() {
        BypassedContextStore.clear();
    }

    @AfterEach
    public void tearDown() {
        BypassedContextStore.clear();
    }

    @Test
    public void testDefaultIsFalse() {
        assertFalse(BypassedContextStore.isBypassed());
    }

    @Test
    public void testSetBypassed() {
        BypassedContextStore.setBypassed(true);
        assertTrue(BypassedContextStore.isBypassed());

        BypassedContextStore.setBypassed(false);
        assertFalse(BypassedContextStore.isBypassed());
    }

    @Test
    public void testClear() {
        BypassedContextStore.setBypassed(true);
        assertTrue(BypassedContextStore.isBypassed());

        BypassedContextStore.clear();
        assertFalse(BypassedContextStore.isBypassed());
    }

    @Test
    public void testThreadIsolation() throws InterruptedException {
        BypassedContextStore.setBypassed(true);
        AtomicBoolean observedInOtherThread = new AtomicBoolean(true);

        Thread t = new Thread(() -> observedInOtherThread.set(BypassedContextStore.isBypassed()));
        t.start();
        t.join();

        assertFalse(observedInOtherThread.get());
        assertTrue(BypassedContextStore.isBypassed());
    }
}
