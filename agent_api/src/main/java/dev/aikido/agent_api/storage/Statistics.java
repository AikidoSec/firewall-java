package dev.aikido.agent_api.storage;

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
}
