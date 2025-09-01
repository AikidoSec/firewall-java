package dev.aikido.agent_api;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.RateLimiterStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.ServiceConfiguration;

import java.util.List;

import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static dev.aikido.agent_api.ratelimiting.RateLimitedEndpointFinder.getRateLimitedEndpoint;

public final class ShouldBlockRequest {
    private ShouldBlockRequest() {
    }

    private static final ShouldBlockRequestResult NO_BLOCK = new ShouldBlockRequestResult(false, null);

    /**
     * shouldBlockRequest() checks user-blocking and rate-limiting.
     * Returns {
     * block: boolean,
     * data: {
     * type: "blocked" | "ratelimited",
     * trigger: "ip" | "user" | "group",
     * ip?: string
     * }
     * }
     */
    public static ShouldBlockRequestResult shouldBlockRequest() {
        ContextObject context = Context.get();
        ServiceConfiguration config = ServiceConfigStore.getConfig();
        if (context == null || config == null) {
            return NO_BLOCK;
        }

        // These indicators allow us to check in core whether the middleware is installed correctly,
        // and to display a warning when a user or group is set after this has run.
        ServiceConfigStore.setMiddlewareInstalled(true);
        context.setExecutedMiddleware(true);
        Context.set(context);

        // User blocking allows customers to easily take action when attacks are coming from specific accounts.
        ShouldBlockRequestResult shouldBlockUser = checkBlockedUser(context, config);
        if (shouldBlockUser != null) {
            return shouldBlockUser;
        }

        List<Endpoint> endpoints = ServiceConfigStore.getConfig().getEndpoints();
        List<Endpoint> matches = matchEndpoints(context.getRouteMetadata(), endpoints);
        Endpoint rateLimitedEndpoint = getRateLimitedEndpoint(matches, context.getRoute());

        // Test rate-limiting (for group, user & ip)
        ShouldBlockRequestResult shouldRateLimit = checkRateLimiting(rateLimitedEndpoint, context);
        if (shouldRateLimit != null) {
            return shouldRateLimit;
        }

        return NO_BLOCK;
    }

    private static ShouldBlockRequestResult checkBlockedUser(ContextObject ctx, ServiceConfiguration config) {
        User currentUser = ctx.getUser();
        if (currentUser == null) {
            return null;
        }

        if (config.isUserBlocked(currentUser.id())) {
            return new ShouldBlockRequestResult(
                /*block*/ true,
                new BlockedRequestResult(
                    /*type*/ "blocked",
                    /*trigger*/ "user",
                    ctx.getRemoteAddress()
                ));
        }

        return null;
    }

    private static ShouldBlockRequestResult checkRateLimiting(Endpoint endpoint, ContextObject ctx) {
        if (endpoint == null) {
            return null;
        }
        long windowSizeInMS = endpoint.getRateLimiting().windowSizeInMS();
        long maxRequests = endpoint.getRateLimiting().maxRequests();
        String key = endpoint.getMethod() + ":" + endpoint.getRoute();
        String remoteAddress = ctx.getRemoteAddress();

        String groupId = ctx.getRateLimitGroup();
        if (groupId != null) {

            // Key is `method:route:group:[Group ID]`
            key += ":group:" + groupId;

            if (RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests)) {
                // We don't check rate-limiting for IP, User if group is set.
                return null;
            }
            return new ShouldBlockRequestResult(
                /*block*/ true,
                new BlockedRequestResult(
                    /*type*/ "ratelimited",
                    /*trigger*/ "group",
                    remoteAddress
                )
            );
        }
        User currentUser = ctx.getUser();
        if (currentUser != null) {

            // Key is `method:route:user:[User ID]`
            key += ":user:" + currentUser.id();

            if (RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests)) {
                // We don't check rate-limiting for IP if user is set.
                return null;
            }
            return new ShouldBlockRequestResult(
                /*block*/ true,
                new BlockedRequestResult(
                    /*type*/ "ratelimited",
                    /*trigger*/ "user",
                    remoteAddress
                )
            );
        }

        if (remoteAddress != null && !remoteAddress.isEmpty()) {

            // Key is `method:route:ip:[IP HERE]`
            key += ":ip:" + remoteAddress;

            if (!RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests)) {
                return new ShouldBlockRequestResult(
                    /*block*/ true,
                    new BlockedRequestResult(
                        /*type*/ "ratelimited",
                        /*trigger*/ "ip",
                        remoteAddress
                    )
                );
            }
        }

        return null;
    }

    public record ShouldBlockRequestResult(boolean block, BlockedRequestResult data) {
    }

    public record BlockedRequestResult(String type, String trigger, String ip) {
    }
}
