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
        assertEquals(1, record.requests().attacksDetected().total());
    }

    @Test
    public void testIncrementAttacksBlocked() {
        String operation = "testOperation";
        StatisticsStore.incrementAttacksBlocked(operation);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.requests().attacksDetected().blocked());
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
        StatisticsStore.incrementIpHits(ip);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.ipAddresses().get("breakdown").get(ip));
    }

    @Test
    public void testIncrementUAHits() {
        String userAgent = "Mozilla/5.0";
        StatisticsStore.incrementUAHits(userAgent);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.userAgents().get("breakdown").get(userAgent));
    }

    @Test
    public void testClear() {
        StatisticsStore.incrementHits();
        StatisticsStore.incrementIpHits("ip");
        StatisticsStore.incrementUAHits("ip");
        String operation = "testOperation";
        StatisticsStore.registerCall(operation, OperationKind.FS_OP);

        StatisticsStore.clear();
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();

        assertNotNull(record);
        assertEquals(0, record.requests().total());
        assertEquals(0, record.operations().size());
        assertEquals(0, record.ipAddresses().get("breakdown").size());
        assertEquals(0, record.userAgents().get("breakdown").size());

    }

    @Test
    public void testIncrementMultipleHits() {
        StatisticsStore.incrementHits();
        StatisticsStore.incrementHits();
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(2, record.requests().total());
    }

    @Test
    public void testIncrementMultipleAttacksDetected() {
        String operation = "testOperation";
        StatisticsStore.incrementAttacksDetected(operation);
        StatisticsStore.incrementAttacksDetected(operation);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(2, record.requests().attacksDetected().total());
    }

    @Test
    public void testIncrementMultipleAttacksBlocked() {
        String operation = "testOperation";
        StatisticsStore.incrementAttacksBlocked(operation);
        StatisticsStore.incrementAttacksBlocked(operation);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(2, record.requests().attacksDetected().blocked());
    }

    @Test
    public void testRegisterMultipleCalls() {
        String operation = "testOperation";
        StatisticsStore.registerCall(operation, OperationKind.FS_OP);
        StatisticsStore.registerCall(operation, OperationKind.FS_OP);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertTrue(record.operations().containsKey(operation));
        assertEquals(2, record.operations().get(operation).total());
    }

    @Test
    public void testIncrementIpHitsMultipleTimes() {
        String ip = "192.168.1.1";
        StatisticsStore.incrementIpHits(ip);
        StatisticsStore.incrementIpHits(ip);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(2, record.ipAddresses().get("breakdown").get(ip));
    }

    @Test
    public void testIncrementUAHitsMultipleTimes() {
        String userAgent = "Mozilla/5.0";
        StatisticsStore.incrementUAHits(userAgent);
        StatisticsStore.incrementUAHits(userAgent);
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(2, record.userAgents().get("breakdown").get(userAgent));
    }

    @Test
    public void testClearAfterMultipleIncrements() {
        StatisticsStore.incrementHits();
        StatisticsStore.incrementIpHits("192.168.1.1");
        StatisticsStore.incrementUAHits("Mozilla/5.0");
        String operation = "testOperation";
        StatisticsStore.registerCall(operation, OperationKind.FS_OP);

        StatisticsStore.clear();
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();

        assertNotNull(record);
        assertEquals(0, record.requests().total());
        assertEquals(0, record.operations().size());
        assertEquals(0, record.ipAddresses().get("breakdown").size());
        assertEquals(0, record.userAgents().get("breakdown").size());
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        Runnable incrementHitsTask = () -> {
            for (int i = 0; i < 100; i++) {
                StatisticsStore.incrementHits();
            }
        };

        Thread thread1 = new Thread(incrementHitsTask);
        Thread thread2 = new Thread(incrementHitsTask);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(200, record.requests().total());
    }

    @Test
    public void testIncrementIpHitsWithDifferentIPs() {
        StatisticsStore.incrementIpHits("192.168.1.1");
        StatisticsStore.incrementIpHits("192.168.1.2");
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.ipAddresses().get("breakdown").get("192.168.1.1"));
        assertEquals(1, record.ipAddresses().get("breakdown").get("192.168.1.2"));
    }

    @Test
    public void testIncrementUAHitsWithDifferentUserAgents() {
        StatisticsStore.incrementUAHits("Mozilla/5.0");
        StatisticsStore.incrementUAHits("Chrome/91.0");

        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.userAgents().get("breakdown").get("Mozilla/5.0"));
        assertEquals(1, record.userAgents().get("breakdown").get("Chrome/91.0"));
    }

    @Test
    public void testClearAfterIncrementingDifferentMetrics() {
        StatisticsStore.incrementHits();
        StatisticsStore.incrementAttacksDetected("operation1");
        StatisticsStore.incrementAttacksBlocked("operation1");
        StatisticsStore.incrementIpHits("192.168.1.1");
        StatisticsStore.incrementUAHits("Mozilla/5.0");

        StatisticsStore.clear();
        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();

        assertNotNull(record);
        assertEquals(0, record.requests().total());
        assertEquals(0, record.requests().attacksDetected().total());
        assertEquals(0, record.requests().attacksDetected().blocked());
        assertEquals(0, record.ipAddresses().get("breakdown").size());
        assertEquals(0, record.userAgents().get("breakdown").size());
    }

    @Test
    public void testConcurrentIncrementHitsAndClear() throws InterruptedException {
        Runnable incrementHitsTask = () -> {
            for (int i = 0; i < 50; i++) {
                StatisticsStore.incrementHits();
            }
        };

        Thread incrementThread = new Thread(incrementHitsTask);
        Thread clearThread = new Thread(() -> {
            try {
                Thread.sleep(100); // Ensure this runs after some hits are incremented
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            StatisticsStore.clear();
        });

        incrementThread.start();
        clearThread.start();
        incrementThread.join();
        clearThread.join();

        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(0, record.requests().total()); // Expecting 0 due to clear
    }

    @Test
    public void testIncrementMultipleMetricsSimultaneously() {
        String ip = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        String operation = "testOperation";

        StatisticsStore.incrementHits();
        StatisticsStore.incrementAttacksDetected(operation);
        StatisticsStore.incrementAttacksBlocked(operation);
        StatisticsStore.incrementIpHits(ip);
        StatisticsStore.incrementUAHits(userAgent);

        Statistics.StatsRecord record = StatisticsStore.getStatsRecord();
        assertNotNull(record);
        assertEquals(1, record.requests().total());
        assertEquals(1, record.requests().attacksDetected().total());
        assertEquals(1, record.requests().attacksDetected().blocked());
        assertNotNull(record.ipAddresses().get("breakdown").get(ip));
        assertEquals(1, record.ipAddresses().get("breakdown").get(ip));
        assertNotNull(record.userAgents().get("breakdown").get(userAgent));
        assertEquals(1, record.userAgents().get("breakdown").get(userAgent));
    }
}
