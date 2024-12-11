package dev.aikido.agent_api.collectors;

import com.google.gson.Gson;
import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.background.ipc_commands.ApiDiscoveryCommand;
import dev.aikido.agent_api.background.utilities.IPCClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;

import static dev.aikido.agent_api.api_discovery.GetApiInfo.getApiInfo;
import static dev.aikido.agent_api.background.utilities.IPCClientFactory.getDefaultIPCClient;
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
        if (statusCode <= 0) {
            return; // Status code below or equal to zero: Invalid request
        }
        ContextObject context = Context.get();
        RouteMetadata routeMetadata = context.getRouteMetadata();
        if (routeMetadata == null || !isUsefulRoute(statusCode, context.getRoute(), context.getMethod())) {
            return;
        }
        IPCClient ipcClient = getDefaultIPCClient();
        Gson gson = new Gson();
        // Report route :
        if (ipcClient != null) {
            String data = "INIT_ROUTE$" + gson.toJson(routeMetadata);
            ipcClient.sendData(data, false /* does not receive a response*/);
        }

        // API Spec code :
        ThreadCacheObject threadCache = ThreadCache.get();
        if (threadCache != null && threadCache.getRoutes() != null) {
            threadCache.getRoutes().initializeRoute(routeMetadata); // Make sure route exists
            RouteEntry route = threadCache.getRoutes().get(routeMetadata);
            route.incrementHits(); // Increment hits so we can limit with constant:
            if (route.getHits() <= ANALYSIS_ON_FIRST_X_REQUESTS && ipcClient != null) {
                APISpec apiSpec = getApiInfo(context);
                ApiDiscoveryCommand.Req req = new ApiDiscoveryCommand.Req(apiSpec, routeMetadata);
                String apiDiscoveryStr = "API_DISCOVERY$" + gson.toJson(req);
                ipcClient.sendData(apiDiscoveryStr, false /* does not receive a response*/);
            }
        }
    }
}