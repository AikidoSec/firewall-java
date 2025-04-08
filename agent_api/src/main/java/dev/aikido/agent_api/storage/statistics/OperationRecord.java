package dev.aikido.agent_api.storage.statistics;

import java.util.HashMap;
import java.util.Map;

public final class OperationRecord {
    private final OperationKind kind;
    private final Map<String, Integer> attacksDetected = new HashMap<>();
    private long total;

    public OperationRecord(OperationKind kind) {
        this.kind = kind;
        this.total = 0;
        this.attacksDetected.put("total", 0);
        this.attacksDetected.put("blocked", 0);
    }

    public void incrementTotal() {
        this.total += 1;
    }

    public long total() {
        return this.total;
    }

    public void incrementAttacksBlocked() {
        this.attacksDetected.compute("blocked", (k, currentBlocked) -> currentBlocked + 1);
    }

    public void incrementAttacksDetected() {
        this.attacksDetected.compute("total", (k, currentTotal) -> currentTotal + 1);
    }

    public Map<String, Integer> getAttacksDetected() {
        return this.attacksDetected;
    }

    public OperationKind getKind() {
        return this.kind;
    }
}
