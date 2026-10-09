package storage;

import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.storage.AttackQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttackQueueTest {
    @BeforeEach
    @AfterEach
    public void clearQueue() {
        AttackQueue.clear();
    }

    @Test
    public void testDropsEventsWhenFull() {
        for (int i = 0; i < 150; i++) {
            AttackQueue.add(new APIEvent() {});
        }
        assertEquals(100, AttackQueue.getSize());
    }
}
