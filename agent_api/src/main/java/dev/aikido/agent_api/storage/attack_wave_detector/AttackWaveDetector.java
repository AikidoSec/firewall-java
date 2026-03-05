package dev.aikido.agent_api.storage.attack_wave_detector;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.ratelimiting.LRUCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.WebScanDetector.isWebScanner;

public class AttackWaveDetector {
    private final LRUCache<String, Integer> suspiciousRequestsCounts;
    private final LRUCache<String, List<Sample>> suspiciousRequestsSamples;
    private final LRUCache<String, Long> sentEventsMap;
    private final int attackWaveThreshold;
    private final int maxSamplesPerIP;

    public AttackWaveDetector() {
        this(
            /* attackWaveThreshold, default: 15 requests */ 15,
            /* attackWaveTimeFrame, default: 1 min */       60_000L,
            /* minTimeBetweenEvents, default: 20 min */     20 * 60_000L,
            /* maxLRUEntries, default: 10,000 entries */    10_000,
            /* maxSamplesPerIP, default: 15 samples */ 15
        );
    }

    /**
     * @param attackWaveThreshold  How many suspicious requests are allowed before triggering an alert
     * @param attackWaveTimeFrame  In what time frame must these requests occur
     * @param minTimeBetweenEvents Minimum time before reporting a new event for the same ip
     * @param maxLRUEntries        Maximum number of entries in the LRU cache
     */
    public AttackWaveDetector(int attackWaveThreshold, long attackWaveTimeFrame,
                              long minTimeBetweenEvents, int maxLRUEntries, int maxSamplesPerIP) {
        this.attackWaveThreshold = attackWaveThreshold;
        this.maxSamplesPerIP = maxSamplesPerIP;
        this.suspiciousRequestsCounts = new LRUCache<>(maxLRUEntries, attackWaveTimeFrame);
        this.suspiciousRequestsSamples = new LRUCache<>(maxLRUEntries, attackWaveTimeFrame);
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
        Integer existingSuspiciousRequests = this.suspiciousRequestsCounts.get(ip);
        if (existingSuspiciousRequests != null) {
            suspiciousRequests = existingSuspiciousRequests + 1;
        }
        this.suspiciousRequestsCounts.set(ip, suspiciousRequests);

        this.trackSample(ip, ctx.getMethod(), ctx.getUrl());

        if (suspiciousRequests < this.attackWaveThreshold) {
            return false;
        }
        this.sentEventsMap.set(ip, System.currentTimeMillis());
        return true;
    }

    public List<Sample> getSamplesForIp(String ip) {
        List<Sample> samples = this.suspiciousRequestsSamples.get(ip);
        if (samples == null) {
            samples = new ArrayList<>();
        }
        return samples;
    }

    public record Sample(String method, String url) {}
    private void trackSample(String ip, String method, String url) {
        List<Sample> samples = getSamplesForIp(ip);
        if (samples.size() >= this.maxSamplesPerIP) {
            return;
        }

        for (Sample sample : samples) {
            if (Objects.equals(sample.method, method) && Objects.equals(sample.url, url)) {
                return; // an equivalent entry already exists, skipping
            }
        }
        samples.add(new Sample(method, url));
        this.suspiciousRequestsSamples.set(ip, samples);
    }
}
