package dev.aikido.agent_api.storage.attack_wave_detector;

import dev.aikido.agent_api.ratelimiting.LRUCache;

public class AttackWaveDetector {
    private final LRUCache<String, Integer> suspiciousRequestsMap;
    private final LRUCache<String, Long> sentEventsMap;
    private final int attackWaveThreshold;

    public AttackWaveDetector() {
        this(15, 60_000L, 20 * 60_000L, 10_000);
    }

    public AttackWaveDetector(int attackWaveThreshold, long attackWaveTimeFrame,
                              long minTimeBetweenEvents, int maxLRUEntries) {
        this.attackWaveThreshold = attackWaveThreshold;
        this.suspiciousRequestsMap = new LRUCache<>(maxLRUEntries, attackWaveTimeFrame);
        this.sentEventsMap = new LRUCache<>(maxLRUEntries, minTimeBetweenEvents);
    }

    /**
     * Checks if the request is part of an attack wave.
     *
     * @param ip           The remote IP address.
     * @param isWebScanner Whether the request is from a web scanner.
     * @return true if an attack wave is detected and should be reported.
     */
    public boolean check(String ip, boolean isWebScanner) {
        if (ip == null) {
            return false;
        }
        Long sentEventTime = this.sentEventsMap.get(ip);
        if (sentEventTime != null) {
            return false;
        }
        if (!isWebScanner) {
            return false;
        }
        int suspiciousRequests = (this.suspiciousRequestsMap.get(ip) != null)
            ? this.suspiciousRequestsMap.get(ip) + 1
            : 1;
        this.suspiciousRequestsMap.set(ip, suspiciousRequests);
        if (suspiciousRequests < this.attackWaveThreshold) {
            return false;
        }
        this.sentEventsMap.set(ip, System.currentTimeMillis());
        return true;
    }

    public void clear() {
        this.suspiciousRequestsMap.clear();
        this.sentEventsMap.clear();
    }
}
