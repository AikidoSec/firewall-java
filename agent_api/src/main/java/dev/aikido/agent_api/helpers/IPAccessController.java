package dev.aikido.agent_api.helpers;

import dev.aikido.agent_api.background.Endpoint;
import java.util.List;
import static dev.aikido.agent_api.helpers.net.LocalhostIP.isLocalhostIP;

public final class IPAccessController {
    private IPAccessController() {}
    public static boolean ipAllowedToAccessRoute(String remoteAddress, List<Endpoint> endpoints) {
        if (remoteAddress != null && isLocalhostIP(remoteAddress)) {
            return true;
        }
        if (endpoints == null || endpoints.isEmpty()) {
            return true;
        }

        for (Endpoint endpoint : endpoints) {
            if (endpoint.getAllowedIPAddresses() == null) {
                // Feature might not be enabled
                continue;
            }
            if (endpoint.getAllowedIPAddresses().isEmpty()) {
                // We will continue to check all the other matches
                continue;
            }

            if (remoteAddress == null) {
                // We only check it here because if allowedIPAddresses isn't set
                // We don't want to change any default behaviour
                return false;
            }

            if (!endpoint.getAllowedIPAddresses().contains(remoteAddress)) {
                // The IP is not in the allowlist, so block
                return false;
            }
        }
        return true;
    }
}
