package dev.aikido.agent_api.storage.statistics;

import dev.aikido.agent_api.helpers.UnixTimeMS;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
    private int totalHits;
    private int attacksDetected;
    private int attacksBlocked;
    private long startedAt;
    private final Map<String, OperationRecord> operations = new HashMap<>();
    public Statistics(int totalHits, int attacksDetected, int attacksBlocked) {
        this.totalHits = totalHits;
        this.attacksDetected = attacksDetected;
        this.attacksBlocked = attacksBlocked;
        this.startedAt = UnixTimeMS.getUnixTimeMS();
    }
    public Statistics() {
        this(0, 0, 0);
    }

    public void incrementTotalHits(int count) {
        totalHits += count;
    }
    public int getTotalHits() { return totalHits; }

    public void incrementAttacksDetected(String operation) {
        this.attacksDetected += 1;
        if (this.operations.containsKey(operation)) {
            this.operations.get(operation).incrementAttacksDetected();
        }
    }
    public int getAttacksDetected() { return attacksDetected; }

    public void incrementAttacksBlocked(String operation) {
        this.attacksBlocked += 1;
        if (this.operations.containsKey(operation)) {
            this.operations.get(operation).incrementAttacksBlocked();
        }
    }
    public int getAttacksBlocked() { return attacksBlocked; }

    public void registerCall(String operation, OperationKind kind) {
        if (!this.operations.containsKey(operation)) {
            this.operations.put(operation, new OperationRecord(kind));
        }
        // increase total count by 1
        this.operations.get(operation).incrementTotal();
    }
    public Map<String, OperationRecord> getOperations() {
        return new HashMap<>(this.operations);
    }

    // Stats records for sending out the heartbeat :
    public record StatsRequestsRecord(long total, long aborted, Map<String, Integer> attacksDetected) {};

    public record StatsRecord(long startedAt, long endedAt, StatsRequestsRecord requests,
                              Map<String, OperationRecord> operations) {
    }

    public StatsRecord getRecord() {
        long endedAt = UnixTimeMS.getUnixTimeMS();
        return new StatsRecord(this.startedAt, endedAt, new StatsRequestsRecord(
                /* total */ totalHits,
                /* aborted */ 0, // Unknown statistic, default to 0,
                /* attacksDetected */ Map.of(
                    "total", attacksDetected,
                    "blocked", attacksBlocked
        )),
                /* operations */ getOperations()
        );
    }

    public void clear() {
        this.totalHits = 0;
        this.attacksBlocked = 0;
        this.attacksDetected = 0;
        this.startedAt = UnixTimeMS.getUnixTimeMS();
        this.operations.clear();
    }
}
