package storage;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.service_configuration.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceConfigStoreTest {

    @BeforeEach
    public void setUp() {
        // Reset to a known state: no domains, blockNewOutgoingRequests=false
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, null, 0L, null, null, null,
                false, null, true, false
        ));
    }

    @Test
    public void testShouldBlockOutgoingRequestNotBlockedByDefault() {
        assertFalse(ServiceConfigStore.shouldBlockOutgoingRequest("example.com"));
    }

    @Test
    public void testShouldBlockOutgoingRequestBlockedDomain() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, null, 0L, null, null, null,
                false,
                List.of(new Domain("blocked.com", "block")),
                true, false
        ));
        assertTrue(ServiceConfigStore.shouldBlockOutgoingRequest("blocked.com"));
    }

    @Test
    public void testShouldBlockOutgoingRequestAllowedDomain() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, null, 0L, null, null, null,
                true,
                List.of(new Domain("allowed.com", "allow")),
                true, false
        ));
        assertFalse(ServiceConfigStore.shouldBlockOutgoingRequest("allowed.com"));
    }

    @Test
    public void testShouldBlockOutgoingRequestUnknownWhenBlockNewEnabled() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, null, 0L, null, null, null,
                true,
                List.of(new Domain("allowed.com", "allow")),
                true, false
        ));
        assertTrue(ServiceConfigStore.shouldBlockOutgoingRequest("unknown.com"));
    }
}
