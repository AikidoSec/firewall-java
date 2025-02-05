package dev.aikido.agent_api.ratelimiting.sliding_window;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds timestamps for requests made to the owner of this object.
 * This class implements a sliding window mechanism to track request timestamps
 * and manage rate limiting based on a specified time window.
 */
public class SlidingWindowEntries {
    // This is a list which holds the UNIX timestamps (in MS) of all requests.
    private final List<Long> entries = new ArrayList<>();
    private final long windowSizeMS;

    public SlidingWindowEntries(long windowSizeMS) {
        this.windowSizeMS = windowSizeMS;
    }
    public void addHit(long currentTime) {
        entries.add(currentTime);
    }
    public int getHitsInWindow(long currentTime) {
        this.clearEntries(currentTime);
        return entries.size(); // Returns all entries that are inside the current time window.
    }

    private void clearEntries(long currentTime) {
        long thresholdTime = currentTime - windowSizeMS;
        entries.removeIf(entry -> entry < thresholdTime); // remove if the entry is too old
    }
}
