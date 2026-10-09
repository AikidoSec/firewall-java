package helpers;

import static org.junit.jupiter.api.Assertions.*;

import dev.aikido.agent_api.helpers.UUIDv7;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UUIDv7Test {
    @Test
    public void testGeneratesUuidV7WithCurrentTimestamp() {
        long before = System.currentTimeMillis();
        UUID uuid = UUIDv7.generate();
        long after = System.currentTimeMillis();

        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());
        long timestamp = uuid.getMostSignificantBits() >>> 16;
        assertTrue(timestamp >= before && timestamp <= after, "unexpected timestamp " + timestamp);
        assertNotEquals(UUIDv7.generate(), UUIDv7.generate());
    }
}
