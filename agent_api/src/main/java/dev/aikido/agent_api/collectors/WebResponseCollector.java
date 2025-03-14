package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.background.ipc_commands.ApiDiscoveryCommand;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;
import dev.aikido.agent_api.background.ipc_commands.InitRouteCommand;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;

import static dev.aikido.agent_api.api_discovery.GetApiInfo.getApiInfo;
import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;
import static dev.aikido.agent_api.helpers.url.IsUsefulRoute.isUsefulRoute;

public final class WebResponseCollector {
    private WebResponseCollector() {}
    // Only do API Discovery on first 20 requests:
    private static final int ANALYSIS_ON_FIRST_X_REQUESTS = 20;
    /**
     * This function gets used after the code of a request is complete, and we have a status code
     * Here we can check if a route is useful, report it, build api specs, ...
     * @param statusCode is the response status code
     */
    public static void report(int statusCode) {
        ContextObject context = Context.get();
        if (statusCode <= 0 || context == null) {
            return; // Status code below or equal to zero: Invalid request
        }
        RouteMetadata routeMetadata = context.getRouteMetadata();
        if (routeMetadata == null || !isUsefulRoute(statusCode, context.getRoute(), context.getMethod())) {
            return;
        }
        ThreadIPCClient threadClient = getDefaultThreadIPCClient();
        ThreadCacheObject threadCache = ThreadCache.get();
        if (threadCache == null || threadClient == null || threadCache.getRoutes() == null) {
            return;
        }
        Routes routes = threadCache.getRoutes();
        RouteEntry currentRoute = routes.get(routeMetadata);
        if (currentRoute == null) {
            // Report route :
            new InitRouteCommand().send(threadClient, routeMetadata);
            routes.initializeRoute(routeMetadata); // Initialize route in thread cache
            currentRoute = routes.get(routeMetadata);
        }

        // API Spec code :
        currentRoute.incrementHits(); // Increment hits so we can limit with constant:
        if (currentRoute.getHits() <= ANALYSIS_ON_FIRST_X_REQUESTS) {
            APISpec apiSpec = getApiInfo(context);
            ApiDiscoveryCommand.Req req = new ApiDiscoveryCommand.Req(apiSpec, routeMetadata);
            new ApiDiscoveryCommand().send(threadClient, req);
        }
    }
}