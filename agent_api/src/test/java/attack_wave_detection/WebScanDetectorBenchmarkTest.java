package attack_wave_detection;

import dev.aikido.agent_api.vulnerabilities.attack_wave_detection.WebScanDetector;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebScanDetectorBenchmarkTest {

    private EmptySampleContextObject getTestContext(String path, String method, String queryTestParam) {
        EmptySampleContextObject context = new EmptySampleContextObject(queryTestParam, path, method);
        context.setIp("::1");
        return context;
    }

    @Test
    void testPerformance() {
        int iterations = 25_000;
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            WebScanDetector.isWebScanner(getTestContext("/wp-config.php", "GET", "1"));
            WebScanDetector.isWebScanner(getTestContext("/vulnerable", "GET", "1'; DROP TABLE users; --"));
            WebScanDetector.isWebScanner(getTestContext("/", "GET", "1"));
        }
        long end = System.nanoTime();
        double timePerCheck = (double) (end - start) / iterations / 3 / 1_000_000; // Convert nanoseconds to milliseconds
        assertTrue(timePerCheck < 0.007,
            String.format("Took %.6fms per check (expected < 0.007ms)", timePerCheck));
    }
}
