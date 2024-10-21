package dev.aikido.AikidoAgent.background;

import java.util.List;

public class Endpoint {
    public record RateLimitingConfig(long maxRequests, long windowSizeInMS) {}
    private final String method;
    private final String route;
    private final RateLimitingConfig rateLimiting;
    private final List<String> allowedIPAddresses;
    private final boolean graphql;
    public Endpoint(String method, String route, long maxRequests, long windowSizeMS, List<String> allowedIPAddresses, boolean graphql) {
        this.method = method;
        this.route = route;
        this.rateLimiting = new RateLimitingConfig(maxRequests, windowSizeMS);
        this.allowedIPAddresses = allowedIPAddresses;
        this.graphql = graphql;
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
    public List<String> getAllowedIPAddresses() {
        return allowedIPAddresses;
    }
    public boolean isGraphql() {
        return graphql;
    }
}
