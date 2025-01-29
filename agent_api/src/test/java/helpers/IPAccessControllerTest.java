package helpers;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.helpers.IPAccessController;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IPAccessControllerTest {
    private Endpoint genEndpoint(List<String> allowedIPAddresses) {
        return new Endpoint(
            /* method */ "POST", /* route */ "/posts/:id",
            /* rlm params */ 0, 0,
            /* Allowed IPs */ allowedIPAddresses, /* graphql */ false,
            /* forceProtectionOff */ true, /* rlm */ false
        );
    }

    @Test
    public void testEmptyEndpoints() {
        assertTrue(IPAccessController.ipAllowedToAccessRoute("1.2.3.4", null));
        assertTrue(IPAccessController.ipAllowedToAccessRoute("1.2.3.4", List.of()));

    }

    @Test
    public void testAlwaysAllowsRequestIfNotProduction() {
        List<Endpoint> endpoints = List.of(genEndpoint(List.of("1.2.3.4")));
        assertTrue(IPAccessController.ipAllowedToAccessRoute("::1", endpoints));
    }

    @Test
    public void testAlwaysAllowsRequestIfNoMatch() {
        List<Endpoint> endpoints = List.of(genEndpoint(List.of("1.2.3.4")));
        assertTrue(IPAccessController.ipAllowedToAccessRoute("1.2.3.4", endpoints));
    }

    @Test
    public void testAlwaysAllowsRequestIfAllowedIpAddress() {
        List<Endpoint> endpoints = List.of(genEndpoint(List.of("1.2.3.4")));
        assertTrue(IPAccessController.ipAllowedToAccessRoute("1.2.3.4", endpoints));
    }

    @Test
    public void testAlwaysAllowsRequestIfLocalhost() {
        List<Endpoint> endpoints = List.of(genEndpoint(List.of("1.2.3.4")));
        assertTrue(IPAccessController.ipAllowedToAccessRoute("::1", endpoints));
    }

    @Test
    public void testBlocksRequestIfNoIpAddress() {
        List<Endpoint> endpoints = List.of(genEndpoint(List.of("1.2.3.4")));
        assertFalse(IPAccessController.ipAllowedToAccessRoute(null, endpoints));
    }

    @Test
    public void testAllowsRequestIfConfigurationIsBroken() {
        List<Endpoint> endpoints = List.of(genEndpoint(Collections.emptyList())); // Broken configuration
        assertTrue(IPAccessController.ipAllowedToAccessRoute("3.4.5.6", endpoints));
    }

    @Test
    public void testAllowsRequestIfAllowedIpAddressesIsEmpty() {
        List<Endpoint> endpoints = List.of(genEndpoint(Collections.emptyList()));
        assertTrue(IPAccessController.ipAllowedToAccessRoute("3.4.5.6", endpoints));
    }

    @Test
    public void testBlocksRequestIfNotAllowedIpAddress() {
        List<Endpoint> endpoints = List.of(genEndpoint(List.of("1.2.3.4")));
        assertFalse(IPAccessController.ipAllowedToAccessRoute("3.4.5.6", endpoints));
    }

    @Test
    public void testChecksEveryMatchingEndpoint() {
        List<Endpoint> endpoints = List.of(
            genEndpoint(List.of("3.4.5.6")),
            genEndpoint(List.of("1.2.3.4"))
        );
        assertFalse(IPAccessController.ipAllowedToAccessRoute("3.4.5.6", endpoints));
    }

    @Test
    public void testIfAllowedIpsIsEmptyOrBroken() {
        List<Endpoint> endpoints = List.of(
            genEndpoint(Collections.emptyList()),
            genEndpoint(Collections.emptyList()), // Broken configuration
            genEndpoint(null), // Broken configuration
            genEndpoint(List.of("1.2.3.4"))
        );

        assertTrue(IPAccessController.ipAllowedToAccessRoute("1.2.3.4", endpoints));
        assertFalse(IPAccessController.ipAllowedToAccessRoute("3.4.5.6", endpoints));
    }
}
