package background.cloud;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.storage.service_configuration.ParsedFirewallLists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SetEnvironmentVariable(key = "AIKIDO_LOG_LEVEL", value = "trace")
public class ReportingAPITest {
    ReportingApiHTTP api;
    @BeforeEach
    public void setup() {
        api = new ReportingApiHTTP("http://localhost:5000/", 2, new Token("token"));
    }

    @Test
    public void testTimeoutValid() {
        api = new ReportingApiHTTP("http://localhost:5000/delayed/2/", 3, new Token("token")); // Allowed delay
        Optional<APIResponse> res = api.fetchNewConfig();
        assertTrue(res.isPresent());
        assertTrue(res.get().block());
        assertEquals(3, res.get().endpoints().size());
    }
    @Test
    public void testTimeoutInvalid() {
        api = new ReportingApiHTTP("http://localhost:5000/delayed/4/", 3, new Token("token")); // Allowed delay
        Optional<APIResponse> res = api.fetchNewConfig();
        assertFalse(res.isPresent());
    }

    @Test
    public void testFetchNewConfig() {
        Optional<APIResponse> res = api.fetchNewConfig();
        assertTrue(res.isPresent());
        assertTrue(res.get().block());
        assertEquals(3, res.get().endpoints().size());
    }
    @Test
    @StdIo
    public void testFetchNewConfigInvalidEndpoint(StdOut out) {
        this.api = new ReportingApiHTTP("http://unknown.app.here:1234/", 2, new Token("token"));
        Optional<APIResponse> res = api.fetchNewConfig();
        assertEquals(Optional.empty(), res);
        assertTrue(
                out.capturedString().contains("DEBUG dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP: Error while fetching new config from cloud"),
                "Failed, string not in " + out.capturedString()
        );
    }

    @Test
    @StdIo
    public void testListsResponseWithWrongEndpoint(StdOut out) {
        this.api = new ReportingApiHTTP("http://unknown.app.here:1234/", 2, new Token("token"));
        Optional<ReportingApiHTTP.APIListsResponse> res = api.fetchBlockedLists();
        assertEquals(Optional.empty(), res);
        assertTrue(
                out.capturedString().contains("DEBUG dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP: Failed to fetch blocked lists"),
                "Failed, string not in " + out.capturedString()
        );
    }
    @Test
    public void testListsResponse() {
        Optional<ReportingApiHTTP.APIListsResponse> res = api.fetchBlockedLists();
        assertTrue(res.isPresent());
        assertEquals(1, res.get().blockedIPAddresses().size());
        assertEquals("geoip", res.get().blockedIPAddresses().get(0).source());
        assertEquals("geo restrictions", res.get().blockedIPAddresses().get(0).description());
        assertEquals("1.2.3.4", res.get().blockedIPAddresses().get(0).ips().get(0));

        assertEquals(1, res.get().monitoredIPAddresses().size());
        assertEquals("geoip", res.get().monitoredIPAddresses().get(0).source());
        assertEquals("should not be blocked", res.get().monitoredIPAddresses().get(0).description());
        assertEquals("5.6.7.8", res.get().monitoredIPAddresses().get(0).ips().get(0));

        assertNull(res.get().allowedIPAddresses());

        assertEquals(3, res.get().userAgentDetails().size());
        assertEquals("ai-agents", res.get().userAgentDetails().get(0).key());
        assertEquals("AI2Bot", res.get().userAgentDetails().get(0).pattern());

        assertEquals("crawlers", res.get().userAgentDetails().get(1).key());
        assertEquals("Bytespider", res.get().userAgentDetails().get(1).pattern());

        assertEquals("crawlers-monitor", res.get().userAgentDetails().get(2).key());
        assertEquals("ClaudeUser", res.get().userAgentDetails().get(2).pattern());

        assertEquals("AI2Bot|Bytespider", res.get().blockedUserAgents());
        assertEquals("ClaudeUser", res.get().monitoredUserAgents());
    }

    @Test
    public void testParsedListsResponse() {
        Optional<ReportingApiHTTP.APIListsResponse> res = api.fetchBlockedLists();
        assertTrue(res.isPresent());

        ParsedFirewallLists parsed = new ParsedFirewallLists();
        parsed.update(res.get());

        var matches = parsed.matchBlockedIps("1.2.3.4");
        assertEquals(1, matches.size());
        assertEquals("geo-1", matches.get(0).key());
        assertEquals("geo restrictions", matches.get(0).description());

        matches = parsed.matchBlockedIps("5.6.7.8");
        assertEquals(0, matches.size());

        matches = parsed.matchMonitoredIps("5.6.7.8");
        assertEquals(1, matches.size());
        assertEquals("geo-2", matches.get(0).key());
        assertEquals("should not be blocked", matches.get(0).description());

        matches = parsed.matchBlockedIps("2.3.4.5");
        assertEquals(0, matches.size());

        var matchUa = parsed.matchBlockedUserAgents("ClaudeUser/2.0 Mozilla");
        assertFalse(matchUa.block());
        assertEquals(1, matchUa.matchedKeys().size());
        assertEquals("crawlers-monitor", matchUa.matchedKeys().get(0));

        matchUa = parsed.matchBlockedUserAgents("ClaudeUser/2.0 Mozilla, AI2Bot");
        assertTrue(matchUa.block());
        assertEquals(2, matchUa.matchedKeys().size());
        assertEquals("ai-agents", matchUa.matchedKeys().get(0));
        assertEquals("crawlers-monitor", matchUa.matchedKeys().get(1));
    }
}
