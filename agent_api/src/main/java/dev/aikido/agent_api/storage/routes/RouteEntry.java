package dev.aikido.agent_api.storage.routes;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.context.RouteMetadata;

import java.io.Serializable;
import java.lang.reflect.Type;

import static dev.aikido.agent_api.api_discovery.APISpecMerger.mergeAPISpecs;

public class RouteEntry implements Serializable {
    final String method;
    final String path;
    private int hits;

    // apispec field is transient because we do not serialize it, since this should not be sent over IPC.
    // We created a RouteEntrySerializer so we can still send it over HTTP
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
    public static class RouteEntrySerializer implements JsonSerializer<RouteEntry> {
        @Override
        public JsonElement serialize(RouteEntry route, Type typeOfSrc, JsonSerializationContext context) {
            // Initialize instances :
            JsonObject jsonObject = new JsonObject();
            Gson gson = new Gson();

            jsonObject.addProperty("method", route.method);
            jsonObject.addProperty("path", route.path);
            jsonObject.addProperty("hits", route.hits);

            // Add API Spec :
            JsonElement apiSpecJson = gson.toJsonTree(route.apispec);
            jsonObject.add("apispec", apiSpecJson);

            return jsonObject;
        }
    }
}
