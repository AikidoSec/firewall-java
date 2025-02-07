package background.cloud;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
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
        api = new ReportingApiHTTP("http://localhost:5000/");
    }

    @Test
    public void testFetchNewConfig() {
        Optional<APIResponse> res = api.fetchNewConfig("token", 2);
        assertTrue(res.isPresent());
        assertTrue(res.get().block());
        assertEquals(3, res.get().endpoints().size());
    }
    @Test
    @StdIo
    public void testFetchNewConfigInvalidEndpoint(StdOut out) {
        this.api = new ReportingApiHTTP("http://unknown.app.here:1234/");
        Optional<APIResponse> res = api.fetchNewConfig("token", 2);
        assertEquals(Optional.empty(), res);
        assertTrue(
                out.capturedString().contains("DEBUG dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP: Error while fetching new config from cloud"),
                "Failed, string not in " + out.capturedString()
        );
    }

    @Test
    public void testListsResponseWithTokenNull() {
        Optional<ReportingApiHTTP.APIListsResponse> res = api.fetchBlockedLists(null);
        assertTrue(res.isEmpty());
    }

    @Test
    @StdIo
    public void testListsResponseWithWrongEndpoint(StdOut out) {
        this.api = new ReportingApiHTTP("http://unknown.app.here:1234/");
        Optional<ReportingApiHTTP.APIListsResponse> res = api.fetchBlockedLists("token");
        assertEquals(Optional.empty(), res);
        assertTrue(
                out.capturedString().contains("DEBUG dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP: Failed to fetch blocked lists"),
                "Failed, string not in " + out.capturedString()
        );
    }
    @Test
    public void testListsResponse() {
        Optional<ReportingApiHTTP.APIListsResponse> res = api.fetchBlockedLists("token");
        assertTrue(res.isPresent());
        assertEquals(1, res.get().blockedIPAddresses().size());
        assertEquals("geoip", res.get().blockedIPAddresses().get(0).source());
        assertEquals("geo restrictions", res.get().blockedIPAddresses().get(0).description());
        assertEquals("1.2.3.4", res.get().blockedIPAddresses().get(0).ips().get(0));
        assertEquals("AI2Bot|Bytespider", res.get().blockedUserAgents());
    }

}
