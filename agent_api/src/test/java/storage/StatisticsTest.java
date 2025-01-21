package storage;

import dev.aikido.agent_api.storage.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticsTest {
    private Statistics stats = null;
    @BeforeEach
    public void setup() {
        stats = new Statistics();
    }
    @Test
    public void testTotalHits() {
        assertEquals(stats.getTotalHits(), 0);
        stats.incrementTotalHits(20);
        assertEquals(stats.getTotalHits(), 20);
        stats.incrementTotalHits(1);
        assertEquals(stats.getTotalHits(), 21);
    }
}
