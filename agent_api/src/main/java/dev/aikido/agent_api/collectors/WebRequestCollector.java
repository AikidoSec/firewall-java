package dev.aikido.agent_api.collectors;

import static dev.aikido.agent_api.helpers.IPAccessController.ipAllowedToAccessRoute;
import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import java.util.List;

public final class WebRequestCollector {
    private WebRequestCollector() {}

    public record Res(String msg, Integer status) {}
    ;

    /**
     * This function gets called in the initial phases of a request.
     * @param newContext is the new ContextObject that holds headers, query, ...
     */
    public static Res report(ContextObject newContext) {
        // Set new context :
        Context.reset();
        Context.set(newContext);
        ThreadCacheObject threadCache = ThreadCache.get();
        if (threadCache == null) {
            return null;
        }

        // Increment total hits :
        threadCache.incrementTotalHits();

        // Blocked IP lists (e.g. Geo restrictions)
        ThreadCacheObject.BlockedResult ipBlocked = threadCache.isIpBlocked(newContext.getRemoteAddress());
        if (ipBlocked.blocked()) {
            String msg = "Your IP address is not allowed to access this resource.";
            msg += " (Your IP: " + newContext.getRemoteAddress() + ")";
            return new Res(msg, 403);
        }
        // Per-route IP allowlists :
        List<Endpoint> matchedEndpoints = matchEndpoints(newContext.getRouteMetadata(), threadCache.getEndpoints());
        if (!ipAllowedToAccessRoute(newContext.getRemoteAddress(), matchedEndpoints)) {
            String msg = "Your IP address is not allowed to access this resource.";
            msg += " (Your IP: " + newContext.getRemoteAddress() + ")";
            return new Res(msg, 403);
        }
        // User-Agent blocking (e.g. blocking bots)
        String userAgent = newContext.getHeaders().get("user-agent");
        if (userAgent != null && !userAgent.isEmpty()) {
            if (threadCache.isBlockedUserAgent(userAgent)) {
                String msg = "You are not allowed to access this resource because you have been identified as a bot.";
                return new Res(msg, 403);
            }
        }
        return null;
    }
}
