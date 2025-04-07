package storage;

import dev.aikido.agent_api.storage.statistics.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatisticsTest {
    private Statistics stats = null;
    @BeforeEach
    public void setup() {
        stats = new Statistics();
    }
    @Test
    public void testTotalHits() {
        assertEquals(0, stats.getTotalHits());
        stats.incrementTotalHits(20);
        assertEquals(20, stats.getTotalHits());
        stats.incrementTotalHits(1);
        assertEquals(21, stats.getTotalHits());
    }

    @Test
    public void testClear() {
        stats.incrementTotalHits(20);
        stats.incrementAttacksBlocked();
        stats.incrementAttacksBlocked();
        stats.incrementAttacksDetected();
        stats.incrementAttacksDetected();
        stats.incrementAttacksDetected();
        assertEquals(3, stats.getAttacksDetected());
        assertEquals(2, stats.getAttacksBlocked());
        assertEquals(20, stats.getTotalHits());

        // Reset :
        stats.clear();

        assertEquals(0, stats.getAttacksBlocked());
        assertEquals(0, stats.getAttacksDetected());
        assertEquals(0, stats.getTotalHits());

    }

    @Test
    public void testConstructor() {
        Statistics stats2 = new Statistics(100, 5, 1);
        assertEquals(100, stats2.getTotalHits());
        assertEquals(5, stats2.getAttacksDetected());
        assertEquals(1, stats2.getAttacksBlocked());
    }

    @Test
    public void testStatsRecord() {
        Statistics stats2 = new Statistics(100, 5, 1);
        Statistics.StatsRecord statsRecord = stats2.getRecord();
        assertEquals(5, statsRecord.requests().attacksDetected().get("total"));
        assertEquals(1, statsRecord.requests().attacksDetected().get("blocked"));
        assertEquals(100, statsRecord.requests().total());

        // Time :
        assertTrue(statsRecord.startedAt() > 0);
        assertTrue(statsRecord.endedAt() > 0);

    }
}
