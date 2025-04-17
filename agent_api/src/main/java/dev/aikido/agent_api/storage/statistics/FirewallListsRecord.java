package dev.aikido.agent_api.storage.statistics;

import java.util.HashMap;
import java.util.Map;

public class FirewallListsRecord {
    private final Map<String, BreakdownEntry> breakdown = new HashMap<>();
    private int total = 0;
    private int blocked = 0;

    public FirewallListsRecord() {
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

    public void incrementTotal() {
        this.total += 1;
    }

    public int getTotal() {
        return this.total;
    }

    public void incrementBlocked() {
        this.blocked += 1;
    }

    public int getBlocked() {
        return this.blocked;
    }

    public void clear() {
        this.total = 0;
        this.blocked = 0;
        this.breakdown.clear();
    }

    record BreakdownEntry(int total, int blocked) {
    }
}
