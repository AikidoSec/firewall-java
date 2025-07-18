package dev.aikido.agent_api.storage.routes;

public final class RouteToKeyHelper {
    private RouteToKeyHelper() {}

    public static String routeToKey(String method, String route) {
        return method + ":" + route;
    }
}
