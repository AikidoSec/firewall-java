package dev.aikido.agent_api.storage.routes;

import com.google.gson.*;
import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.context.RouteMetadata;

import java.lang.reflect.Type;

import static dev.aikido.agent_api.api_discovery.APISpecMerger.mergeAPISpecs;

public class RouteEntry {
    final String method;
    final String path;
    private int hits;

    // apispec field is transient because we do not send it over IPC.
    // We created a RouteEntrySerializer so we can still send it over HTTP
    public transient APISpec apispec;

    // deltaHits field is transient because it's only meant to be local.
    private transient int deltaHits = 0;

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
