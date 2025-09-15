package storage;

import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.OperationRecord;
import dev.aikido.agent_api.storage.statistics.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        stats.registerCall("test1", OperationKind.FS_OP);
        stats.incrementAttacksBlocked("test1");
        stats.incrementAttacksBlocked("test2");
        stats.incrementAttacksDetected("test2");
        stats.incrementAttacksDetected("test1");
        stats.incrementAttacksDetected("test1");
        stats.incrementRateLimitStats();
        stats.incrementRateLimitStats();
        assertEquals(3, stats.getAttacksDetected());
        assertEquals(2, stats.getAttacksBlocked());
        assertEquals(20, stats.getTotalHits());
        assertEquals(2, stats.getOperations().get("test1").getAttacksDetected().get("total"));
        assertEquals(1, stats.getOperations().get("test1").getAttacksDetected().get("blocked"));
        assertEquals(2, stats.getRateLimitedStats());

        assertFalse(stats.getOperations().containsKey("test2"));
        // Reset :
        stats.clear();

        assertEquals(0, stats.getAttacksBlocked());
        assertEquals(0, stats.getAttacksDetected());
        assertEquals(0, stats.getTotalHits());
        assertEquals(0, stats.getRateLimitedStats());

    }

    @Test
    public void testConstructor() {
        Statistics stats2 = new Statistics(100, 5, 1);
        assertEquals(100, stats2.getTotalHits());
        assertEquals(5, stats2.getAttacksDetected());
        assertEquals(1, stats2.getAttacksBlocked());
        assertEquals(0, stats.getRateLimitedStats());
    }

    @Test
    public void testStatsRecord() {
        Statistics stats2 = new Statistics(100, 5, 1);
        stats2.registerCall("operation1", OperationKind.FS_OP);
        Statistics.StatsRecord statsRecord = stats2.getRecord();
        assertEquals(5, statsRecord.requests().attacksDetected().total());
        assertEquals(1, statsRecord.requests().attacksDetected().blocked());
        assertEquals(0, statsRecord.requests().attackWaves().total());
        assertEquals(0, statsRecord.requests().attackWaves().blocked());
        assertEquals(100, statsRecord.requests().total());
        assertEquals(1, statsRecord.operations().get("operation1").total());
        assertEquals(1, statsRecord.operations().size());

        // Time :
        assertTrue(statsRecord.startedAt() > 0);
        assertTrue(statsRecord.endedAt() > 0);

    }

    @Test
    public void testRegisterCall() {
        stats.registerCall("operation1", OperationKind.FS_OP);
        stats.registerCall("operation1", OperationKind.EXEC_OP);
        stats.registerCall("operation2", OperationKind.EXEC_OP);

        Map<String, OperationRecord> operations = stats.getOperations();
        assertEquals(2, operations.get("operation1").total());
        assertEquals(1, operations.get("operation2").total());
    }

    @Test
    public void testGetOperationsImmutable() {
        stats.registerCall("operation1", OperationKind.FS_OP);
        Map<String, OperationRecord> operations = stats.getOperations();

        // Ensure that the returned map is not modifiable
        operations.put("operation2", new OperationRecord(OperationKind.EXEC_OP));
        assertEquals(1, stats.getOperations().size()); // Should still only have operation1
    }

    @Test
    public void testIncrementAttackWavesDetected() {
        assertEquals(0, stats.getAttackWavesDetected());
        stats.incrementAttackWavesDetected();
        assertEquals(1, stats.getAttackWavesDetected());
        stats.incrementAttackWavesDetected();
        assertEquals(2, stats.getAttackWavesDetected());
    }

    @Test
    public void testIncrementAttackWavesBlocked() {
        assertEquals(0, stats.getAttackWavesBlocked());
        stats.incrementAttackWavesBlocked();
        assertEquals(1, stats.getAttackWavesBlocked());
        stats.incrementAttackWavesBlocked();
        assertEquals(2, stats.getAttackWavesBlocked());
    }

    @Test
    public void testAttackWavesStatsAfterClear() {
        stats.incrementAttackWavesDetected();
        stats.incrementAttackWavesDetected();
        stats.incrementAttackWavesBlocked();
        assertEquals(2, stats.getAttackWavesDetected());
        assertEquals(1, stats.getAttackWavesBlocked());
        stats.clear();
        assertEquals(0, stats.getAttackWavesDetected());
        assertEquals(0, stats.getAttackWavesBlocked());
    }

    @Test
    public void testAttackWavesStatsInConstructor() {
        Statistics stats2 = new Statistics(0, 3, 2);
        assertEquals(3, stats2.getAttacksDetected());
        assertEquals(2, stats2.getAttacksBlocked());
        // Ensure new stats are zero
        assertEquals(0, stats2.getAttackWavesDetected());
        assertEquals(0, stats2.getAttackWavesBlocked());
    }

    @Test
    public void testAttackWavesStatsRecord() {
        stats.incrementAttackWavesDetected();
        stats.incrementAttackWavesDetected();
        stats.incrementAttackWavesBlocked();
        Statistics.StatsRecord statsRecord = stats.getRecord();
        assertEquals(2, statsRecord.requests().attackWaves().total());
        assertEquals(1, statsRecord.requests().attackWaves().blocked());
    }
}
