package dev.aikido.agent_api.storage;

import java.util.Map;

public class Statistics {
    private int totalHits;
    private int attacksDetected;
    private int attacksBlocked;
    public Statistics(int totalHits, int attacksDetected, int attacksBlocked) {
        this.totalHits = totalHits;
        this.attacksDetected = attacksDetected;
        this.attacksBlocked = attacksBlocked;
    }
    public Statistics() {
        this(0, 0, 0);
    }

    public void incrementTotalHits(int count) {
        totalHits += count;
    }
    public int getTotalHits() { return totalHits; }
    public int getAttacksDetected() { return attacksDetected; }
    public int getAttacksBlocked() { return attacksBlocked; }

    public void incrementAttacksDetected() {
        this.attacksDetected += 1;
    }
    public void incrementAttacksBlocked() {
        this.attacksBlocked += 1;
    }

    // Stats records for sending out the heartbeat :
    public record StatsRequestsRecord(long total, long aborted, Map<String, Integer> attacksDetected) {};
    public record StatsRecord(long startedAt, long endedAt, StatsRequestsRecord requests) {};
    public StatsRecord getRecord() {
        return new StatsRecord(0, 0, new StatsRequestsRecord(
                /* total */ totalHits,
                /* aborted */ 0, // Unknown statistic, default to 0,
                /* attacksDetected */ Map.of(
                    "total", attacksDetected,
                    "blocked", attacksBlocked
                )
        ));
    }
}
