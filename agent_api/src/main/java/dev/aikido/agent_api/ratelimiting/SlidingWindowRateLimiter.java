package dev.aikido.agent_api.ratelimiting;

import java.util.ArrayList;
import java.util.List;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class SlidingWindowRateLimiter implements RateLimiter {
    private final LRUCache<String, List<Long>> rateLimitedItems;

    public SlidingWindowRateLimiter(int maxItems, long timeToLiveInMs) {
        this.rateLimitedItems = new LRUCache<String, List<Long>>(maxItems, timeToLiveInMs);
    }

    @Override
    public boolean isAllowed(String key, long windowSizeInMs, long maxRequests) {
        long currentTime = getUnixTimeMS();
        List<Long> requestTimestamps = rateLimitedItems.get(key);
        if (requestTimestamps == null) {
            requestTimestamps = new ArrayList<>();
        }

        // clear entries that are not in the rate-limiting window anymore
        requestTimestamps.removeIf(entry -> entry < currentTime - windowSizeInMs);

        // Ensure the number of entries exceeds maxRequests by only 1
        while (requestTimestamps.size() > (maxRequests+1)) {
            // We remove the oldest entry, since this one has become useless if the limit is already exceeded
            requestTimestamps.remove(0);
        }

        // Update entries by adding the new timestamp and storing it in the LRU Cache
        requestTimestamps.add(currentTime);
        rateLimitedItems.set(key, requestTimestamps);

        // If the total amount of requests in the current window exceeds max requests, we rate-limit
        return requestTimestamps.size() <= maxRequests;
    }
}
