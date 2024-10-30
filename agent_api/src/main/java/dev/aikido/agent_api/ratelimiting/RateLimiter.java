package dev.aikido.agent_api.ratelimiting;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class RateLimiter {
    private final LRUCache<String, RequestInfo> rateLimitedItems;

    public RateLimiter(int maxItems, long timeToLiveInMs) {
        this.rateLimitedItems = new LRUCache<String, RequestInfo>(maxItems, timeToLiveInMs);
    }

    public boolean isAllowed(String key, long windowSizeInMs, long maxRequests) {
        long currentTime = getUnixTimeMS();
        RequestInfo requestInfo = rateLimitedItems.get(key);

        if (requestInfo == null) {
            // New item, set and allow.
            rateLimitedItems.set(key, new RequestInfo(1, currentTime));
            return true;
        }

        long elapsedTime = currentTime - requestInfo.startTime;

        if (elapsedTime > windowSizeInMs) {
            // Reset the counter and timestamp if windowSizeInMs has expired
            rateLimitedItems.set(key, new RequestInfo(1, currentTime));
            return true;
        }

        if (requestInfo.count < maxRequests) {
            // Increment the counter if it is within the windowSizeInMs and maxRequests
            requestInfo.count++;
            rateLimitedItems.set(key, requestInfo); // Update the cache
            return true;
        }

        // Deny the request if the maxRequests is reached within windowSizeInMs
        return false;
    }

    private static class RequestInfo {
        int count;
        long startTime;

        RequestInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }
    }
}
