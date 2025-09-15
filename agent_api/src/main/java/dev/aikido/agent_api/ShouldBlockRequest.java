package dev.aikido.agent_api;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.ratelimiting.ShouldRateLimit;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.ServiceConfiguration;
import dev.aikido.agent_api.storage.routes.RoutesStore;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;

public final class ShouldBlockRequest {
    private ShouldBlockRequest() {
    }
    private static final ShouldBlockRequestResult NO_BLOCK = new ShouldBlockRequestResult(false, null);

    /**
     * shouldBlockRequest() checks user-blocking and rate-limiting.
     * Returns {
     *      block: boolean,
     *      data: {
     *          type: "blocked" | "ratelimited",
     *          trigger: "ip" | "user" | "group",
     *          ip?: string
     *      }
     *  }
     */
    public static ShouldBlockRequestResult shouldBlockRequest() {
        ContextObject context = Context.get();
        ServiceConfiguration config = ServiceConfigStore.getConfig();
        if (context == null || config == null) {
            return NO_BLOCK;
        }

        // These indicators allow us to check in core whether the middleware is installed correctly,
        // and to display a warning when a user or group is set after this has run.
        context.setExecutedMiddleware(true);
        ServiceConfigStore.setMiddlewareInstalled(true);
        Context.set(context);

        /* User blocking allows customers to easily take action when attacks are coming from specific accounts. */
        User currentUser = context.getUser();
        if (currentUser != null) {
            if (config.isUserBlocked(currentUser.id())) {
                BlockedRequestResult blockedRequestResult = new BlockedRequestResult(
                    /* type */    "blocked",
                    /* trigger */ "user",
                    /* ip */      context.getRemoteAddress()
                );
                return new ShouldBlockRequestResult(true, blockedRequestResult);
            }
        }

        // Rate-limiting for group, user, or IP.
        ShouldRateLimit.RateLimitDecision rateLimitDecision = ShouldRateLimit.shouldRateLimit(
            context.getRouteMetadata(), context.getUser(), context.getRateLimitGroup(), context.getRemoteAddress()
        );
        if (rateLimitDecision.block()) {
            // increment rate-limiting stats both globally and on the route :
            StatisticsStore.incrementRateLimited();
            // increment routes stats using method & route from the endpoint (store stats for wildcards, in wildcard route)
            RoutesStore.addRouteRateLimitedCount(
                rateLimitDecision.rateLimitedEndpoint().getMethod(),
                rateLimitDecision.rateLimitedEndpoint().getRoute()
            );

            BlockedRequestResult blockedRequestResult = new BlockedRequestResult(
                /* type */    "ratelimited",
                /* trigger */ rateLimitDecision.trigger(),
                /* ip */      context.getRemoteAddress()
            );
            return new ShouldBlockRequestResult(true, blockedRequestResult);
        }

        return NO_BLOCK;
    }

    public record ShouldBlockRequestResult(boolean block, BlockedRequestResult data) {
    }

    public record BlockedRequestResult(String type, String trigger, String ip) {
    }
}
