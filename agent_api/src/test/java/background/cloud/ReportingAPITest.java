package background.cloud;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.helpers.env.Token;
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
        assertEquals(2, res.get().blockedIPAddresses().size());
        assertEquals("geoip", res.get().blockedIPAddresses().get(0).source());
        assertEquals("geo restrictions", res.get().blockedIPAddresses().get(0).description());
        assertEquals("1.2.3.4", res.get().blockedIPAddresses().get(0).ips().get(0));
        assertEquals(3, res.get().blockedUserAgents().size());
        assertEquals("AI2Bot", res.get().blockedUserAgents().get(0).pattern());
        assertEquals("Bytespider", res.get().blockedUserAgents().get(1).pattern());

    }
}
