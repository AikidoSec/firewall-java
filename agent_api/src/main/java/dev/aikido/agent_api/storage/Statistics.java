package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.helpers.UnixTimeMS;
import java.util.Map;

public class Statistics {
    private int totalHits;
    private int attacksDetected;
    private int attacksBlocked;
    private long startedAt;

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

    public int getTotalHits() {
        return totalHits;
    }

    public int getAttacksDetected() {
        return attacksDetected;
    }

    public int getAttacksBlocked() {
        return attacksBlocked;
    }

    public void incrementAttacksDetected() {
        this.attacksDetected += 1;
    }

    public void incrementAttacksBlocked() {
        this.attacksBlocked += 1;
    }

    // Stats records for sending out the heartbeat :
    public record StatsRequestsRecord(long total, long aborted, Map<String, Integer> attacksDetected) {}
    ;

    public record StatsRecord(long startedAt, long endedAt, StatsRequestsRecord requests) {}
    ;

    public StatsRecord getRecord() {
        long endedAt = UnixTimeMS.getUnixTimeMS();
        return new StatsRecord(
                this.startedAt,
                endedAt,
                new StatsRequestsRecord(
                        /* total */ totalHits,
                        /* aborted */ 0, // Unknown statistic, default to 0,
                        /* attacksDetected */ Map.of(
                                "total", attacksDetected,
                                "blocked", attacksBlocked)));
    }

    public void clear() {
        this.totalHits = 0;
        this.attacksBlocked = 0;
        this.attacksDetected = 0;
        this.startedAt = UnixTimeMS.getUnixTimeMS();
    }
}
