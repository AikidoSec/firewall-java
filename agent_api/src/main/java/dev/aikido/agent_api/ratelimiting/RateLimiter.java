package dev.aikido.agent_api.ratelimiting;

public interface RateLimiter {
    void clear();
    public boolean isAllowed(String key, long windowSizeInMs, long maxRequests);
}
