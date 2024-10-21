package dev.aikido.AikidoAgent.background;

public class Endpoint {
    public record RateLimitingConfig(long maxRequests, long windowSizeInMS) {}
    private final String method;
    private final String route;
    private final RateLimitingConfig rateLimiting;
    public Endpoint(String method, String route, long maxRequests, long windowSizeMS) {
        this.method = method;
        this.route = route;
        this.rateLimiting = new RateLimitingConfig(maxRequests, windowSizeMS);
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
}
