package storage;

import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.OperationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OperationRecordTest {

    private OperationRecord operationRecord;

    @BeforeEach
    public void setUp() {
        operationRecord = new OperationRecord(OperationKind.SQL_OP);
    }

    @Test
    public void testInitialization() {
        assertEquals(OperationKind.SQL_OP, operationRecord.getKind());
        assertEquals(0, operationRecord.total());
        Map<String, Integer> attacksDetected = operationRecord.getAttacksDetected();
        assertEquals(0, attacksDetected.get("total"));
        assertEquals(0, attacksDetected.get("blocked"));
    }

    @Test
    public void testIncrementTotal() {
        operationRecord.incrementTotal();
        assertEquals(1, operationRecord.total());

        operationRecord.incrementTotal();
        assertEquals(2, operationRecord.total());
    }

    @Test
    public void testIncrementAttacksBlocked() {
        operationRecord.incrementAttacksBlocked();
        Map<String, Integer> attacksDetected = operationRecord.getAttacksDetected();
        assertEquals(0, attacksDetected.get("total"));
        assertEquals(1, attacksDetected.get("blocked"));

        operationRecord.incrementAttacksBlocked();
        assertEquals(2, attacksDetected.get("blocked"));
    }

    @Test
    public void testIncrementAttacksDetected() {
        operationRecord.incrementAttacksDetected();
        Map<String, Integer> attacksDetected = operationRecord.getAttacksDetected();
        assertEquals(1, attacksDetected.get("total"));
        assertEquals(0, attacksDetected.get("blocked"));

        operationRecord.incrementAttacksDetected();
        assertEquals(2, attacksDetected.get("total"));
    }

    @Test
    public void testMultipleOperations() {
        operationRecord.incrementTotal();
        operationRecord.incrementAttacksDetected();
        operationRecord.incrementAttacksBlocked();

        assertEquals(1, operationRecord.total());
        Map<String, Integer> attacksDetected = operationRecord.getAttacksDetected();
        assertEquals(1, attacksDetected.get("total"));
        assertEquals(1, attacksDetected.get("blocked"));
    }
}
