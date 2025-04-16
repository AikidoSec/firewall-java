package dev.aikido.agent_api.storage.statistics;

public class FirewallListsRecord {
    private int total = 0;
    private int blocked = 0;

    public FirewallListsRecord() {
    }

    public int getTotal() {
        return this.total;
    }

    public void incrementTotal() {
        this.total += 1;
    }

    public int getBlocked() {
        return this.blocked;
    }

    public void incrementBlocked() {
        this.blocked += 1;
    }
}
