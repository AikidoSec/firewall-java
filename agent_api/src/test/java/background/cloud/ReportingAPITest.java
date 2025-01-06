package background.cloud;

import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ReportingAPITest {
    ReportingApiHTTP api;
    @BeforeEach
    public void setup() {
        api = new ReportingApiHTTP("http://localhost:5000/");
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

    @Test
    public void testListsResponseWithTokenNull() {
        Optional<ReportingApiHTTP.APIListsResponse> res = api.fetchBlockedLists(null);
        assertTrue(res.isEmpty());
    }

}
