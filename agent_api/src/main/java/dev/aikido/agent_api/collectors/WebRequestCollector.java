package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;

import java.util.List;

import static dev.aikido.agent_api.helpers.IPAccessController.ipAllowedToAccessRoute;
import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static dev.aikido.agent_api.storage.ServiceConfigStore.getServiceConfig;

public final class WebRequestCollector {
    private WebRequestCollector() {}
    public record Res(String msg, Integer status) {};

    /**
     * This function gets called in the initial phases of a request.
     * @param newContext is the new ContextObject that holds headers, query, ...
     * Also returns a response depending on blocking status :
     * 1) First check: IP allowed to access route (restricted /admin routes e.g.)
     * 2) Bypassed IP? If bypassed, no further checks
     * 3) Blocked IP Lists: e.g. geo-blocking, crowdsec, ...
     * 4) UA Blocking: e.g. bot blocking, AI scraper blocking, ...
     */
    public static Res report(ContextObject newContext) {
        // Set new context :
        Context.reset();
        Context.set(newContext);
        ThreadCacheObject threadCache = ThreadCache.get();
        ServiceConfiguration config = getServiceConfig();
        if (threadCache == null) {
            return null;
        }

        // Increment total hits :
        threadCache.incrementTotalHits();

        // Per-route IP allowlists :
        List<Endpoint> matchedEndpoints = matchEndpoints(newContext.getRouteMetadata(), threadCache.getEndpoints());
        if (!ipAllowedToAccessRoute(newContext.getRemoteAddress(), matchedEndpoints)) {
            String msg = "Your IP address is not allowed to access this resource.";
            msg += " (Your IP: " + newContext.getRemoteAddress() + ")";
            return new Res(msg, 403);
        }

        // add check for bypassed ips (after IP Allowlist check)
        if (config.isBypassedIP(newContext.getRemoteAddress())) {
            return null;
        }

        // Blocked IP lists (e.g. Geo restrictions)
        ThreadCacheObject.BlockedResult ipBlocked = threadCache.isIpBlocked(newContext.getRemoteAddress());
        if (ipBlocked.blocked()) {
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
