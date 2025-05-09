package dev.aikido.agent_api.storage.statistics;

import dev.aikido.agent_api.helpers.UnixTimeMS;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
    private final Map<String, OperationRecord> operations = new HashMap<>();
    private final Map<String, Integer> ipAddressMatches = new HashMap<>();
    private final Map<String, Integer> userAgentMatches = new HashMap<>();
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


    // hits
    public void incrementTotalHits(int count) {
        totalHits += count;
    }

    public int getTotalHits() {
        return totalHits;
    }


    // attack stats
    public void incrementAttacksDetected(String operation) {
        this.attacksDetected += 1;
        if (this.operations.containsKey(operation)) {
            this.operations.get(operation).incrementAttacksDetected();
        }
    }

    public int getAttacksDetected() {
        return attacksDetected;
    }

    public void incrementAttacksBlocked(String operation) {
        this.attacksBlocked += 1;
        if (this.operations.containsKey(operation)) {
            this.operations.get(operation).incrementAttacksBlocked();
        }
    }

    public int getAttacksBlocked() {
        return attacksBlocked;
    }


    // operations
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


    // firewall lists
    public Map<String, Integer> getIpAddresses() {
        return new HashMap<>(this.ipAddressMatches);
    }

    public Map<String, Integer> getUserAgents() {
        return new HashMap<>(this.userAgentMatches);
    }

    public void addMatchToIpAddresses(String key) {
        if (this.ipAddressMatches.containsKey(key)) {
            int currentHitCount = this.ipAddressMatches.get(key);
            this.ipAddressMatches.put(key, currentHitCount + 1);
        } else {
            this.ipAddressMatches.put(key, 0);
        }
    }

    public void addMatchToUserAgents(String key) {
        if (this.userAgentMatches.containsKey(key)) {
            int currentHitCount = this.userAgentMatches.get(key);
            this.userAgentMatches.put(key, currentHitCount + 1);
        } else {
            this.userAgentMatches.put(key, 0);
        }
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
            getOperations(),
            Map.of("breakdown", getIpAddresses()),
            Map.of("breakdown", getUserAgents())
        );
    }

    public void clear() {
        this.totalHits = 0;
        this.attacksBlocked = 0;
        this.attacksDetected = 0;
        this.startedAt = UnixTimeMS.getUnixTimeMS();
        this.operations.clear();
        this.ipAddressMatches.clear();
        this.userAgentMatches.clear();
    }

    // Stats records for sending out the heartbeat :
    public record StatsRequestsRecord(long total, long aborted, Map<String, Integer> attacksDetected) {
    }

    public record StatsRecord(long startedAt, long endedAt, StatsRequestsRecord requests,
                              Map<String, OperationRecord> operations,
                              Map<String, Map<String, Integer>> ipAddresses,
                              Map<String, Map<String, Integer>> userAgents) {
    }
}
