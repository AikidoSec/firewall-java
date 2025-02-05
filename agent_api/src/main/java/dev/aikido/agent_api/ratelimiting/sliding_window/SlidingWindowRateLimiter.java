package dev.aikido.agent_api.ratelimiting.sliding_window;

import dev.aikido.agent_api.ratelimiting.LRUCache;
import dev.aikido.agent_api.ratelimiting.RateLimiter;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class SlidingWindowRateLimiter implements RateLimiter {
    private final LRUCache<String, SlidingWindowEntries> rateLimitedItems;

    public SlidingWindowRateLimiter(int maxItems, long timeToLiveInMs) {
        this.rateLimitedItems = new LRUCache<String, SlidingWindowEntries>(maxItems, timeToLiveInMs);
    }

    @Override
    public boolean isAllowed(String key, long windowSizeInMs, long maxRequests) {
        long currentTime = getUnixTimeMS();
        SlidingWindowEntries entries = rateLimitedItems.get(key);
        if (entries == null) {
            // Create new item
            entries = new SlidingWindowEntries(windowSizeInMs);
            rateLimitedItems.set(key, entries);
        }

        entries.addHit(currentTime); // Add a new hit.

        return entries.getHitsInWindow(currentTime) <= maxRequests;
    }
}
