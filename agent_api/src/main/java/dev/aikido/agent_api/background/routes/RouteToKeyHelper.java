package dev.aikido.agent_api.background.routes;

import dev.aikido.agent_api.context.RouteMetadata;

public class RouteToKeyHelper {
    public static String routeToKey(RouteMetadata routeMetadata) {
        return routeMetadata.method() + ":" + routeMetadata.route();
    }
}
