package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.ServiceConfiguration;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;

import java.util.List;

import static dev.aikido.agent_api.helpers.IPAccessController.ipAllowedToAccessRoute;
import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static dev.aikido.agent_api.storage.ServiceConfigStore.getConfig;

public final class WebRequestCollector {
    private WebRequestCollector() {
    }

    /**
     * This function gets called in the initial phases of a request.
     *
     * @param newContext is the new ContextObject that holds headers, query, ...
     *                   Also returns a response depending on blocking status :
     *                   1) If the IP is bypassed we just won't set the context object
     *                   2) IP allowed to access route (restricted /admin routes e.g.)
     *                   3) Blocked IP Lists: e.g. geo-blocking, crowdsec, ...
     *                   4) UA Blocking: e.g. bot blocking, AI scraper blocking, ...
     */
    public static Res report(ContextObject newContext) {
        ServiceConfiguration config = getConfig();
        Context.reset(); // clear context
        if (config.isIpBypassed(newContext.getRemoteAddress())) {
            return null; // do not set context when the IP address is bypassed (zen = off)
        }

        Context.set(newContext);

        // Increment total hits :
        StatisticsStore.incrementHits();

        Res endpointAllowlistRes = checkEndpointAllowlist(newContext.getRouteMetadata(), newContext.getRemoteAddress(), config);
        if (endpointAllowlistRes != null)
            return endpointAllowlistRes;

        Res blockedIpsRes = checkBlockedIps(newContext.getRemoteAddress());
        if (blockedIpsRes != null)
            return blockedIpsRes;

        return checkBlockedUserAgents(newContext.getHeader("user-agent"));
    }

    private static Res checkEndpointAllowlist(RouteMetadata routeMetadata, String remoteAddress, ServiceConfiguration config) {
        List<Endpoint> matchedEndpoints = matchEndpoints(routeMetadata, config.getEndpoints());
        if (!ipAllowedToAccessRoute(remoteAddress, matchedEndpoints)) {
            String msg = "Your IP address is not allowed to access this resource.";
            msg += " (Your IP: " + remoteAddress + ")";
            return new Res(msg, 403);
        }
        return null; // not blocked
    }

    private static Res checkBlockedIps(String remoteAddress) {
        ServiceConfiguration.BlockedResult ipBlocked = ServiceConfigStore.isIpBlocked(remoteAddress);
        if (ipBlocked.blocked()) {
            String msg = "Your IP address is blocked. Reason: " + ipBlocked.description();
            msg += " (Your IP: " + remoteAddress + ")";
            return new Res(msg, 403);
        }
        return null; // not blocked
    }

    private static Res checkBlockedUserAgents(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null; // not blocked
        }
        if (ServiceConfigStore.isBlockedUserAgent(userAgent)) {
            String msg = "You are not allowed to access this resource because you have been identified as a bot.";
            return new Res(msg, 403);
        }
        return null; // not blocked
    }

    public record Res(String msg, Integer status) {
    }
}
