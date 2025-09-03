package attack_wave_detection;

import dev.aikido.agent_api.storage.attack_wave_detector.AttackWaveDetector;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackWaveDetectorTest {

    private AttackWaveDetector newAttackWaveDetector() {
        // Use much smaller time frames for testing (e.g., 100ms instead of 60s)
        return new AttackWaveDetector(6, 100L, 200L, 10_000);
    }

    private static boolean checkDetector(AttackWaveDetector detector, String ip, boolean isWebScanner) {
        EmptySampleContextObject ctx = new EmptySampleContextObject();
        if (isWebScanner) {
            ctx = new EmptySampleContextObject("../etc/passwd", "/wp-config.php", "BADMETHOD");
        }
        ctx.setIp(ip);
        // debug
        System.out.println(ip);
        return detector.check(ctx);
    }

    @Test
    void testNoIpAddress() throws InterruptedException {
        AttackWaveDetector detector = newAttackWaveDetector();
        assertFalse(checkDetector(detector, null, true));
    }

    @Test
    void testNotAWebScanner() throws InterruptedException {
        AttackWaveDetector detector = newAttackWaveDetector();
        assertFalse(checkDetector(detector, "::1", false));
        assertFalse(checkDetector(detector, "::1", false));
        assertFalse(checkDetector(detector, "::1", false));
        assertFalse(checkDetector(detector, "::1", false));
        assertFalse(checkDetector(detector, "::1", false));
        assertFalse(checkDetector(detector, "::1", false));
    }

    @Test
    void testWebScanner() throws InterruptedException {
        AttackWaveDetector detector = newAttackWaveDetector();
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertTrue(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
    }

    @Test
    void testWebScannerWithDelays() throws InterruptedException {
        AttackWaveDetector detector = newAttackWaveDetector();
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));

        // Small delay (50ms)
        Thread.sleep(50);
        assertFalse(checkDetector(detector, "::1", true));
        assertTrue(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));

        // Wait for minTimeBetweenEvents (200ms)
        Thread.sleep(205);
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertTrue(checkDetector(detector, "::1", true));
    }

    @Test
    void testSlowWebScannerSecondInterval() throws InterruptedException {
        AttackWaveDetector detector = newAttackWaveDetector();
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));

        // Small delay, move time frame (102ms)
        Thread.sleep(102);
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertTrue(checkDetector(detector, "::1", true));
    }

    @Test
    void testSlowWebScannerThirdInterval() throws InterruptedException {
        AttackWaveDetector detector = newAttackWaveDetector();
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));

        // Small delay, move time frame (102ms)
        Thread.sleep(102);
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));

        // Small delay, move time frame (102ms)
        Thread.sleep(102);
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertFalse(checkDetector(detector, "::1", true));
        assertTrue(checkDetector(detector, "::1", true));
    }
}
