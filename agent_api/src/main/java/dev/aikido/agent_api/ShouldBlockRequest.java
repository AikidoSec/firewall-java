package dev.aikido.agent_api;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.ratelimiting.ShouldRateLimit;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.ServiceConfiguration;

public final class ShouldBlockRequest {
    private ShouldBlockRequest() {
    }

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
        if (context == null) {
            return new ShouldBlockRequestResult(false, null); // Blocking false
        }
        context.setExecutedMiddleware(true); // Mark middleware as executed.
        ServiceConfigStore.setMiddlewareInstalled(true);
        Context.set(context);

        // check for blocked user ids
        if (context.getUser() != null) {
            if (config.isUserBlocked(context.getUser().id())) {
                return new ShouldBlockRequestResult(/*block*/ true, new BlockedRequestResult(
                        /*type*/ "blocked",/*trigger*/ "user", context.getRemoteAddress()
                ));
            }
        }

        // Rate-limiting :
        ShouldRateLimit.RateLimitDecision rateLimitDecision = ShouldRateLimit.shouldRateLimit(
                context.getRouteMetadata(), context.getUser(), context.getRemoteAddress()
        );
        if (rateLimitDecision.block()) {
            BlockedRequestResult blockedRequestResult = new BlockedRequestResult(
                    "ratelimited", rateLimitDecision.trigger(), context.getRemoteAddress()
            );
            return new ShouldBlockRequestResult(true, blockedRequestResult);
        }

        return new ShouldBlockRequestResult(false, null); // Blocking false
    }

    public record ShouldBlockRequestResult(boolean block, BlockedRequestResult data) {
    }

    public record BlockedRequestResult(String type, String trigger, String ip) {
    }
}
