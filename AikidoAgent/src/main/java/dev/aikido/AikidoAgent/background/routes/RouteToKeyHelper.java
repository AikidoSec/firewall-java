package dev.aikido.AikidoAgent.background.routes;

import dev.aikido.AikidoAgent.context.RouteMetadata;

public class RouteToKeyHelper {
    public static String routeToKey(RouteMetadata routeMetadata) {
        return routeMetadata.method() + ":" + routeMetadata.route();
    }
}
