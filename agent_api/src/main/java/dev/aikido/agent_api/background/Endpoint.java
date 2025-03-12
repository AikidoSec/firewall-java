package dev.aikido.agent_api.background;

import dev.aikido.agent_api.helpers.net.IPList;

import java.util.List;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPList;

public class Endpoint {
    public record RateLimitingConfig(long maxRequests, long windowSizeInMS, boolean enabled) {}
    private final String method;
    private final String route;
    private final RateLimitingConfig rateLimiting;
    private final List<String> allowedIPAddresses;
    private final boolean graphql;
    private final boolean forceProtectionOff;
    public Endpoint(
            String method, String route, long maxRequests,
            long windowSizeMS, List<String> allowedIPAddresses, boolean graphql,
            boolean forceProtectionOff, boolean rateLimitingEnabled) {
        this.method = method;
        this.route = route;
        this.allowedIPAddresses = allowedIPAddresses;
        this.rateLimiting = new RateLimitingConfig(maxRequests, windowSizeMS, rateLimitingEnabled);
        this.graphql = graphql;
        this.forceProtectionOff = forceProtectionOff;
    }

    // Getters :
    public String getMethod() {
        return method;
    }
    public String getRoute() {
        return route;
    }
    public RateLimitingConfig getRateLimiting() {
        return rateLimiting;
    }
    public boolean isGraphql() {
        return graphql;
    }
    public boolean protectionForcedOff() {
        return forceProtectionOff;
    }

    // allowed ip addresses :
    public boolean allowedIpAddressesEmpty() {
        return allowedIPAddresses == null || allowedIPAddresses.size() == 0;
    }

    public boolean isIpAllowed(String ip) {
        return createIPList(allowedIPAddresses).matches(ip);
    }
}
