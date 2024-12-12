package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static dev.aikido.agent_api.helpers.IPAccessController.ipAllowedToAccessRoute;
import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;

public final class WebRequestCollector {
    private WebRequestCollector() {}
    public record Res(String msg, Integer status) {};
    public static final Logger logger = LogManager.getLogger(WebRequestCollector.class);

    /**
     * This function gets called in the initial phases of a request.
     * @param newContext is the new ContextObject that holds headers, query, ...
     */
    public static Res report(ContextObject newContext) {
        // Set new context :
        Context.reset();
        Context.set(newContext);

        // Check if IP is allowed :
        ThreadCacheObject threadCache = ThreadCache.get();
        /*
        if (threadCache != null) {
            List<Endpoint> matchedEndpoints = matchEndpoints(newContext.getRouteMetadata(), threadCache.getEndpoints());
            if (!ipAllowedToAccessRoute(newContext.getRemoteAddress(), matchedEndpoints)) {
                String msg = "Your IP address is not allowed to access this resource.";
                msg += " (Your IP: " + newContext.getRemoteAddress() + ")";
                return new Res(msg, 403);
            }
        }*/
        return null;
    }
}
