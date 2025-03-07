package dev.aikido.agent_api.ratelimiting;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public interface RateLimiter {
    public boolean isAllowed(String key, long windowSizeInMs, long maxRequests);
}
