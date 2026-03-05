package vulnerabilities.outbound_blocking;

import dev.aikido.agent_api.storage.service_configuration.Domain;
import dev.aikido.agent_api.vulnerabilities.outbound_blocking.OutboundDomains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OutboundDomainsTest {

    private OutboundDomains outboundDomains;

    @BeforeEach
    public void setUp() {
        outboundDomains = new OutboundDomains();
    }

    // --- shouldBlockOutgoingRequest with blockNewOutgoingRequests=false (default) ---

    @Test
    public void testDefaultDoesNotBlockUnknownHostname() {
        assertFalse(outboundDomains.shouldBlockOutgoingRequest("unknown.com"));
    }

    @Test
    public void testBlockModeBlocksHostname() {
        outboundDomains.update(List.of(new Domain("blocked.com", "block")), false);
        assertTrue(outboundDomains.shouldBlockOutgoingRequest("blocked.com"));
    }

    @Test
    public void testAllowModeDoesNotBlockHostname() {
        outboundDomains.update(List.of(new Domain("allowed.com", "allow")), false);
        assertFalse(outboundDomains.shouldBlockOutgoingRequest("allowed.com"));
    }

    @Test
    public void testUnknownHostnameNotBlockedWhenBlockNewOutgoingRequestsFalse() {
        outboundDomains.update(List.of(new Domain("blocked.com", "block")), false);
        assertFalse(outboundDomains.shouldBlockOutgoingRequest("unknown.com"));
    }

    // --- shouldBlockOutgoingRequest with blockNewOutgoingRequests=true ---

    @Test
    public void testUnknownHostnameBlockedWhenBlockNewOutgoingRequestsTrue() {
        outboundDomains.update(List.of(), true);
        assertTrue(outboundDomains.shouldBlockOutgoingRequest("unknown.com"));
    }

    @Test
    public void testAllowModeNotBlockedWhenBlockNewOutgoingRequestsTrue() {
        outboundDomains.update(List.of(new Domain("allowed.com", "allow")), true);
        assertFalse(outboundDomains.shouldBlockOutgoingRequest("allowed.com"));
    }

    @Test
    public void testBlockModeBlockedWhenBlockNewOutgoingRequestsTrue() {
        outboundDomains.update(List.of(new Domain("blocked.com", "block")), true);
        assertTrue(outboundDomains.shouldBlockOutgoingRequest("blocked.com"));
    }

    // --- update() behaviour ---

    @Test
    public void testUpdateWithNullDomainsPreservesExistingDomains() {
        outboundDomains.update(List.of(new Domain("blocked.com", "block")), false);
        // null domains should not reset the map
        outboundDomains.update(null, false);
        assertTrue(outboundDomains.shouldBlockOutgoingRequest("blocked.com"));
    }

    @Test
    public void testUpdateWithNullDomainsUpdatesBlockFlag() {
        outboundDomains.update(null, true);
        // blockNewOutgoingRequests should now be true even though domains unchanged
        assertTrue(outboundDomains.shouldBlockOutgoingRequest("unknown.com"));
    }

    @Test
    public void testUpdateReplacesDomainsMap() {
        outboundDomains.update(List.of(new Domain("old.com", "block")), false);
        outboundDomains.update(List.of(new Domain("new.com", "block")), false);
        // old entry should be gone
        assertFalse(outboundDomains.shouldBlockOutgoingRequest("old.com"));
        assertTrue(outboundDomains.shouldBlockOutgoingRequest("new.com"));
    }

    @Test
    public void testUpdateWithEmptyListClearsDomainsMap() {
        outboundDomains.update(List.of(new Domain("blocked.com", "block")), false);
        outboundDomains.update(List.of(), false);
        assertFalse(outboundDomains.shouldBlockOutgoingRequest("blocked.com"));
    }

    @Test
    public void testMultipleDomainsWithMixedModes() {
        outboundDomains.update(List.of(
                new Domain("blocked.com", "block"),
                new Domain("allowed.com", "allow")
        ), false);
        assertTrue(outboundDomains.shouldBlockOutgoingRequest("blocked.com"));
        assertFalse(outboundDomains.shouldBlockOutgoingRequest("allowed.com"));
        assertFalse(outboundDomains.shouldBlockOutgoingRequest("other.com"));
    }
}
