package dev.aikido.agent_api.collectors;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.utilities.IPCDefaultClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;

import static dev.aikido.agent_api.helpers.url.IsUsefulRoute.isUsefulRoute;

public class WebResponseCollector {
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
        boolean currentRouteUseful = isUsefulRoute(statusCode, context.getRoute(), context.getMethod());
        if (currentRouteUseful) {
            Gson gson = new Gson();
            String data = "INIT_ROUTE$" + gson.toJson(context.getRouteMetadata());
            new IPCDefaultClient().sendData(data, false /* does not receive a response*/);
        }
    }
}