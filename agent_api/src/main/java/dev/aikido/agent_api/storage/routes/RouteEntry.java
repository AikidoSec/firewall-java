package dev.aikido.agent_api.storage.routes;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.context.RouteMetadata;

import java.io.Serializable;

import static dev.aikido.agent_api.api_discovery.APISpecMerger.mergeAPISpecs;

public class RouteEntry implements Serializable {
    final String method;
    final String path;
    private int hits;
    public transient APISpec apispec;

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

    public void updateApiSpec(APISpec newApiSpec) {
        this.apispec = mergeAPISpecs(newApiSpec, this.apispec);
    }
}
