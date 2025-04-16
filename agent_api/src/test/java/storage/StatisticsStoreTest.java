package storage;

import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.Statistics;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticsStoreTest {

    @BeforeEach
    public void setUp() {
        // Clear the statistics before each test
        StatisticsStore.clear();
    }

    @Test
    public void testIncrementHits() {
        StatisticsStore.incrementHits();
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.requests().total());
    }

    @Test
    public void testIncrementAttacksDetected() {
        String operation = "testOperation";
        StatisticsStore.incrementAttacksDetected(operation);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.requests().attacksDetected().get("total"));
    }

    @Test
    public void testIncrementAttacksBlocked() {
        String operation = "testOperation";
        StatisticsStore.incrementAttacksBlocked(operation);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.requests().attacksDetected().get("blocked"));
    }

    @Test
    public void testRegisterCall() {
        String operation = "testOperation";
        StatisticsStore.registerCall(operation, OperationKind.FS_OP);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertTrue(record.operations().containsKey(operation));
        assertEquals(1, record.operations().get(operation).total());
    }

    @Test
    public void testIncrementIpHits() {
        String ip = "192.168.1.1";
        StatisticsStore.incrementIpHits(ip, false);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertTrue(record.ipAddresses().containsKey(ip));
        assertEquals(1, record.ipAddresses().get(ip).getTotal());
    }

    @Test
    public void testIncrementUAHits() {
        String userAgent = "Mozilla/5.0";
        StatisticsStore.incrementUAHits(userAgent, true);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertTrue(record.userAgents().containsKey(userAgent));
        assertEquals(1, record.userAgents().get(userAgent).getTotal());
        assertEquals(1, record.userAgents().get(userAgent).getBlocked());
    }

    @Test
    public void testClear() {
        StatisticsStore.incrementHits();
        StatisticsStore.incrementIpHits("ip", false);
        StatisticsStore.incrementUAHits("ip", false);
        String operation = "testOperation";
        StatisticsStore.registerCall(operation, OperationKind.FS_OP);

        StatisticsStore.clear();
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();

        assertNotNull(record);
        assertEquals(0, record.requests().total());
        assertEquals(0, record.operations().size());
        assertEquals(0, record.ipAddresses().size());
        assertEquals(0, record.userAgents().size());

    }
}
