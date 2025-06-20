package dev.aikido.agent_api.storage.routes;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.context.RouteMetadata;

import static dev.aikido.agent_api.api_discovery.APISpecMerger.mergeAPISpecs;

public class RouteEntry {
    final String method;
    final String path;
    private int hits;
    private int rateLimitedCount;
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

    public void incrementRateLimitCount() {
        rateLimitedCount++;
    }

    public int getRateLimitCount() {
        return rateLimitedCount;
    }
    public void updateApiSpec(APISpec newApiSpec) {
        this.apispec = mergeAPISpecs(newApiSpec, this.apispec);
    }
}
