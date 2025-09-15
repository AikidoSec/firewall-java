package dev.aikido.agent_api.storage.attack_wave_detector;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.ratelimiting.LRUCache;

import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.WebScanDetector.isWebScanner;

public class AttackWaveDetector {
    private final LRUCache<String, Integer> suspiciousRequestsMap;
    private final LRUCache<String, Long> sentEventsMap;
    private final int attackWaveThreshold;

    public AttackWaveDetector() {
        this(
            /* attackWaveThreshold, default: 15 requests */ 15,
            /* attackWaveTimeFrame, default: 1 min */       60_000L,
            /* minTimeBetweenEvents, default: 20 min */     20 * 60_000L,
            /* maxLRUEntries, default: 10,000 entries */    10_000
        );
    }

    /**
     * @param attackWaveThreshold  How many suspicious requests are allowed before triggering an alert
     * @param attackWaveTimeFrame  In what time frame must these requests occur
     * @param minTimeBetweenEvents Minimum time before reporting a new event for the same ip
     * @param maxLRUEntries        Maximum number of entries in the LRU cache
     */
    public AttackWaveDetector(int attackWaveThreshold, long attackWaveTimeFrame,
                              long minTimeBetweenEvents, int maxLRUEntries) {
        this.attackWaveThreshold = attackWaveThreshold;
        this.suspiciousRequestsMap = new LRUCache<>(maxLRUEntries, attackWaveTimeFrame);
        this.sentEventsMap = new LRUCache<>(maxLRUEntries, minTimeBetweenEvents);
    }

    /**
     * Checks if the request is part of an attack wave.
     *
     * @param ctx the context object to check.
     * @return true if an attack wave is detected and should be reported.
     */
    public boolean check(ContextObject ctx) {
        String ip = ctx.getRemoteAddress();
        if (ip == null) {
            return false;
        }

        Long sentEventTime = this.sentEventsMap.get(ip);
        if (sentEventTime != null) {
            // the last attack wave was detected too recently, so don't send another event for a while.
            return false;
        }

        if (!isWebScanner(ctx)) {
            return false;
        }

        // Add 1 to the suspiciousRequests counter.
        int suspiciousRequests = 1;
        Integer existingSuspiciousRequests = this.suspiciousRequestsMap.get(ip);
        if (existingSuspiciousRequests != null) {
            suspiciousRequests = existingSuspiciousRequests + 1;
        }
        this.suspiciousRequestsMap.set(ip, suspiciousRequests);

        if (suspiciousRequests < this.attackWaveThreshold) {
            return false;
        }
        this.sentEventsMap.set(ip, System.currentTimeMillis());
        return true;
    }
}
