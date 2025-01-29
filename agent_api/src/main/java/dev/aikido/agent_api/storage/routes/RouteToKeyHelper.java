package dev.aikido.agent_api.storage.routes;

import dev.aikido.agent_api.context.RouteMetadata;

public final class RouteToKeyHelper {
    private RouteToKeyHelper() {
    }

    public static String routeToKey(RouteMetadata routeMetadata) {
        return routeMetadata.method() + ":" + routeMetadata.route();
    }
}
