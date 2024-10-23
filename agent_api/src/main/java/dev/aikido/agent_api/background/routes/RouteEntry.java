package dev.aikido.agent_api.background.routes;

import dev.aikido.agent_api.context.RouteMetadata;

public class RouteEntry {
    private final String method;
    private final String path;
    private int hits;

    public RouteEntry(String method, String path) {
        this.method = method;
        this.path = path;
        this.hits = 0;
    }
    public RouteEntry(RouteMetadata routeMetadata) {
        this(routeMetadata.method(), routeMetadata.route());
    }

    public void incrementHits() {
        hits++;
    }

    public int getHits() {
        return hits;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
