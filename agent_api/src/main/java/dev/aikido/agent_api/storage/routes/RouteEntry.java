package dev.aikido.agent_api.storage.routes;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.context.RouteMetadata;

public class RouteEntry {
    private final String method;
    private final String path;
    private int hits;
    private APISpec apispec;

    public RouteEntry(String method, String path) {
        this.method = method;
        this.path = path;
        this.hits = 0;
        this.apispec = null;
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

    public APISpec updateApiSpec(APISpec newApiSpec) {
        this.apispec = newApiSpec;
    }
    public APISpec getApispec() {
        return apispec;
    }
}
