package context;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ContextTest {

    @AfterEach
    void tearDown() {
        // Ensure the ThreadLocal is reset after each test
        Context.reset();
    }

    @Test
    void testSetAndGet() {
        ContextObject contextObject = new ContextObject();
        Context.set(contextObject);

        ContextObject retrieved = Context.get();
        Assertions.assertNotNull(retrieved);
    }

    @Test
    void testGetWithoutSet() {
        ContextObject retrieved = Context.get();
        Assertions.assertNull(retrieved, "Expected null when no context is set");
    }

    @Test
    void testReset() {
        ContextObject contextObject = new ContextObject();
        Context.set(contextObject);
        Context.reset();

        ContextObject retrieved = Context.get();
        Assertions.assertNull(retrieved, "Expected null after reset");
    }
}
