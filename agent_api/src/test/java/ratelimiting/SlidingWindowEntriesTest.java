package ratelimiting;

import dev.aikido.agent_api.ratelimiting.sliding_window.SlidingWindowEntries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowEntriesTest {
    private SlidingWindowEntries slidingWindowEntries;
    private final long windowSizeMS = 1000; // 1 second window
    private final long maxRequests = 5; // Allow 5 requests in the window

    @BeforeEach
    void setUp() {
        slidingWindowEntries = new SlidingWindowEntries(windowSizeMS, maxRequests);
    }

    @Test
    void testAddHitAndGetHitsInWindow() {
        long currentTime = System.currentTimeMillis();
        slidingWindowEntries.addHit(currentTime);
        assertEquals(1, slidingWindowEntries.getHitsInWindow(currentTime)); // Should return 1 hit
    }

    @Test
    void testGetHitsInWindowWithinLimit() {
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            slidingWindowEntries.addHit(currentTime + (i * 100)); // Add hits at 0ms, 100ms, 200ms
        }
        assertEquals(3, slidingWindowEntries.getHitsInWindow(currentTime + 500)); // Should return 3 hits
    }

    @Test
    void testGetHitsInWindowAfterClearingOldEntries() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            slidingWindowEntries.addHit(currentTime + (i * 100)); // Add hits at 0ms, 100ms, 200ms, 300ms, 400ms
        }
        assertEquals(5, slidingWindowEntries.getHitsInWindow(currentTime + 1000)); // Should return 2 hits
        slidingWindowEntries.addHit(currentTime + (5 * 100));
        slidingWindowEntries.addHit(currentTime + (5 * 100));
        assertEquals(6, slidingWindowEntries.getHitsInWindow(currentTime + 1000)); // Should return 2 hits

        // Now we should only have the last 2 hits (300ms and 400ms)
        assertEquals(2, slidingWindowEntries.getHitsInWindow(currentTime + 1500)); // Should return 2 hits
    }

    @Test
    void testGetHitsInWindowExceedingMaxRequests() {
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < 7; i++) {
            slidingWindowEntries.addHit(currentTime + (i * 100)); // Add hits at 0ms, 100ms, ..., 600ms
        }

        // After adding 7 hits, we should only keep the last 6
        assertEquals(6, slidingWindowEntries.getHitsInWindow(currentTime + 1000)); // Should return 5 hits
    }

    @Test
    void testSlidingWindowBehavior() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            slidingWindowEntries.addHit(currentTime + (i * 100)); // Add hits at 0ms, 100ms, 200ms, 300ms, 400ms
        }

        // Sleep for 500ms to allow the first hits to slide out of the window
        TimeUnit.MILLISECONDS.sleep(500);

        // Now we should still have 5 hits, as the window is sliding
        assertEquals(5, slidingWindowEntries.getHitsInWindow(currentTime + 1000)); // Should return 5 hits

        // Sleep for another 600ms to allow the first batch to expire
        TimeUnit.MILLISECONDS.sleep(600);

        // Now we should only have 0 hits
        assertEquals(0, slidingWindowEntries.getHitsInWindow(currentTime + 2000)); // Should return 0 hits
    }
}
