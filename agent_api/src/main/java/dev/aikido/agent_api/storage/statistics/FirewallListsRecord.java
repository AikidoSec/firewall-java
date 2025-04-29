package dev.aikido.agent_api.storage.statistics;

import java.util.HashMap;
import java.util.Map;

public class FirewallListsRecord {
    private final Map<String, BreakdownEntry> breakdown = new HashMap<>();

    public FirewallListsRecord() {
    }

    public FirewallListsRecord(FirewallListsRecord previous) {
        this.breakdown.putAll(previous.breakdown);
    }

    public void increment(String key, boolean blocked) {
        breakdown.computeIfAbsent(key, k -> new BreakdownEntry(0, 0));

        int newTotal = breakdown.get(key).total + 1;
        int newBlocked = breakdown.get(key).blocked;
        if (blocked) {
            newBlocked += 1;
        }

        breakdown.put(key, new BreakdownEntry(newTotal, newBlocked));
    }

    public BreakdownEntry get(String key) {
        return this.breakdown.get(key);
    }

    public void clear() {
        this.breakdown.clear();
    }

    public int size() {
        return this.breakdown.size();
    }

    public record BreakdownEntry(int total, int blocked) {
    }
}
