package thread_cache;

import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class ThreadCacheObjectTest {
    @Test
    public void update() {
        ThreadCacheObject tCache = new ThreadCacheObject(null, null, null, null, Optional.of(new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "description", List.of(
                        "1.2.3.4",
                        "192.168.2.1/24",
                        "fd00:1234:5678:9abc::1",
                        "fd00:3234:5678:9abc::1/64",
                        "5.6.7.8/32"
                ))
        ), null, "Test|One")));

        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("1.2.3.4"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("2.3.4.5"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("192.168.2.2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:1234:5678:9abc::1"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("fd00:1234:5678:9abc::2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:3234:5678:9abc::1"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:3234:5678:9abc::2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("5.6.7.8"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("1.2"));
        assertTrue(tCache.isBlockedUserAgent("This is my TEST user agent"));
        assertTrue(tCache.isBlockedUserAgent("Test"));
        assertTrue(tCache.isBlockedUserAgent("TEst and ONE"));
        assertFalse(tCache.isBlockedUserAgent("Est|On"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Linux; Android 10; Pixel 3 XL) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Mobile Safari/537.36"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Firefox/89.0"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Linux; Android 11; Samsung Galaxy S21) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Mobile Safari/537.36"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/18.18363"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Linux; Ubuntu; X11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Opera/77.0.4054.90"));
    }

    @Test
    public void updateEmpty() {
        ThreadCacheObject tCache = new ThreadCacheObject(null, null, null, null, Optional.of(new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "description", List.of(
                        "1.2.3.4",
                        "192.168.2.1/24",
                        "fd00:1234:5678:9abc::1",
                        "fd00:3234:5678:9abc::1/64",
                        "5.6.7.8/32"
                ))
        ), null, "Test|One")));

        tCache.updateBlockedLists(Optional.of(new ReportingApi.APIListsResponse(null,  null,null)));

        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("1.2.3.4"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("2.3.4.5"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("192.168.2.2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:1234:5678:9abc::1"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("fd00:1234:5678:9abc::2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:3234:5678:9abc::1"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:3234:5678:9abc::2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("5.6.7.8"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("1.2"));
        assertTrue(tCache.isBlockedUserAgent("This is my TEST user agent"));
        assertTrue(tCache.isBlockedUserAgent("Test"));
        assertTrue(tCache.isBlockedUserAgent("TEst and ONE"));
        assertFalse(tCache.isBlockedUserAgent("Est|On"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Linux; Android 10; Pixel 3 XL) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Mobile Safari/537.36"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Firefox/89.0"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Linux; Android 11; Samsung Galaxy S21) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Mobile Safari/537.36"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/18.18363"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Linux; Ubuntu; X11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Opera/77.0.4054.90"));
    }

    @Test
    public void updateRegexes() {
        ThreadCacheObject tCache = new ThreadCacheObject(null, null, null, null, Optional.of(new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "description", List.of(
                        "1.2.3.4",
                        "192.168.2.1/24",
                        "fd00:1234:5678:9abc::1",
                        "fd00:3234:5678:9abc::1/64",
                        "5.6.7.8/32"
                ))
        ), null, "Test|One")));

        tCache.updateBlockedLists(Optional.of(new ReportingApi.APIListsResponse(null, null, "")));

        assertTrue(tCache.isBlockedUserAgent("This is my TEST user agent"));
        assertTrue(tCache.isBlockedUserAgent("Test"));
        assertTrue(tCache.isBlockedUserAgent("TEst and ONE"));
        assertFalse(tCache.isBlockedUserAgent("Est|On"));
        assertFalse(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"));
        tCache.updateBlockedLists(Optional.of(new ReportingApi.APIListsResponse(null, null, "Mozilla")));

        assertFalse(tCache.isBlockedUserAgent("This is my TEST user agent"));
        assertFalse(tCache.isBlockedUserAgent("Test"));
        assertFalse(tCache.isBlockedUserAgent("TEst and ONE"));
        assertFalse(tCache.isBlockedUserAgent("Est|On"));
        assertTrue(tCache.isBlockedUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"));
    }

    @Test
    public void testThreadCacheHits() {
        ThreadCacheObject threadCacheObject = getEmptyThreadCacheObject();
        assertEquals(0, threadCacheObject.getTotalHits());
        assertEquals(0, threadCacheObject.getTotalHits());
        threadCacheObject.incrementTotalHits();
        threadCacheObject.incrementTotalHits();
        assertEquals(2, threadCacheObject.getTotalHits());
        assertEquals(2, threadCacheObject.getTotalHits());
        threadCacheObject.incrementTotalHits();
        assertEquals(3, threadCacheObject.getTotalHits());
    }

    @Test
    public void testIsMiddlewareInstalled() {
        ThreadCacheObject threadCacheObject = getEmptyThreadCacheObject();
        assertFalse(threadCacheObject.isMiddlewareInstalled());
        assertFalse(threadCacheObject.isMiddlewareInstalled());
        threadCacheObject.setMiddlewareInstalled();
        assertTrue(threadCacheObject.isMiddlewareInstalled());
        threadCacheObject.setMiddlewareInstalled();
        assertTrue(threadCacheObject.isMiddlewareInstalled());
    }
    @Test
    public void testThreadCacheBypassedIPs() {
        ThreadCacheObject tCache = getEmptyThreadCacheObject(Set.of("1.2.3.4", "5.6.7.8"));
        assertTrue(tCache.isBypassedIP("1.2.3.4"));
        assertFalse(tCache.isBypassedIP("1.2.3.5"));
        assertTrue(tCache.isBypassedIP("5.6.7.8"));
        assertFalse(tCache.isBypassedIP("5.6.7.9"));
    }

    @Test
    public void testThreadCacheBypassedIPsSubnet() {
        ThreadCacheObject tCache = getEmptyThreadCacheObject(Set.of("10.0.0.0/24"));
        assertTrue(tCache.isBypassedIP("10.0.0.200"));
        assertTrue(tCache.isBypassedIP("10.0.0.1"));
        assertTrue(tCache.isBypassedIP("10.0.0.255"));
        assertFalse(tCache.isBypassedIP("10.0.1.1"));
        assertFalse(tCache.isBypassedIP("1.2.3.4"));
    }

    @Test
    public void testIsIpBlockedWithAllowedAndBlockedIPs() {
        // Create a ThreadCacheObject with both allowed and blocked IPs
        ThreadCacheObject tCache = new ThreadCacheObject(null, null, null, null, Optional.of(new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "description", List.of(
                        "1.2.3.4", // Blocked IP
                        "192.168.1.1" // Blocked IP
                ))
        ), List.of(
                new ReportingApi.ListsResponseEntry("geoip", "description", List.of(
                        "10.0.0.1", // Allowed IP
                        "1.2.3.4"
                ))
        ), "Test|One")));

        // Test blocked IPs
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("1.2.3.4"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("192.168.1.1"));

        // Test allowed IPs
        /// Private IP :
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("10.0.0.2"));
        /// Not in allowlist
        assertEquals(new ThreadCacheObject.BlockedResult(true, "not in allowlist"), tCache.isIpBlocked("1.2.3.3"));
    }

    @Test
    public void testIsIpBlockedWithOnlyAllowedIPs() {
        // Create a ThreadCacheObject with only allowed IPs
        ThreadCacheObject tCache = new ThreadCacheObject(null, null, null, null, Optional.of(new ReportingApi.APIListsResponse(null, List.of(
                new ReportingApi.ListsResponseEntry("geoip", "description", List.of(
                        "10.0.0.1" // Allowed IP
                ))
        ), "Test|One")));

        // Test allowed IP
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("10.0.0.1"));
        // Test a non-allowed private-IP
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("10.0.0.2"));
        // Test a non-allowed IP
        assertEquals(new ThreadCacheObject.BlockedResult(true, "not in allowlist"), tCache.isIpBlocked("1.2.3.4"));
    }

    @Test
    public void testIsIpBlockedWithOnlyBlockedIPs() {
        // Create a ThreadCacheObject with only blocked IPs
        ThreadCacheObject tCache = new ThreadCacheObject(null, null, null, null, Optional.of(new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "description", List.of(
                        "1.2.3.4", // Blocked IP
                        "192.168.1.1" // Blocked IP
                ))
        ), null, "Test|One")));

        // Test blocked IPs
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("1.2.3.4"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("192.168.1.1"));
        // Test a non-blocked IP
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("10.0.0.1"));
    }

    @Test
    public void testIsIpBlockedWithAllowedIPsAndBlockedIPs() {
        // Create a ThreadCacheObject with multiple allowed and blocked IPs
        ThreadCacheObject tCache = new ThreadCacheObject(null, null, null, null, Optional.of(new ReportingApi.APIListsResponse(null, List.of(
                new ReportingApi.ListsResponseEntry("geoip1", "description", List.of(
                        "1.2.3.4" // Blocked IP
                )),
                new ReportingApi.ListsResponseEntry("geoip2", "description", List.of(
                        "8.8.8.0/24"
                )),
                new ReportingApi.ListsResponseEntry("geoip3", "description", List.of(
                        "4.4.4.4" // Another allowed IP
                ))
        ), "Test|One")));

        // Test allowed IPs
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("10.0.0.1"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("4.4.4.4"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("1.2.3.4"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("8.8.8.1"));

        // Test a non-allowed IP
        assertEquals(new ThreadCacheObject.BlockedResult(true, "not in allowlist"), tCache.isIpBlocked("4.4.4.1"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "not in allowlist"), tCache.isIpBlocked("8.8.7.8"));

    }


}
