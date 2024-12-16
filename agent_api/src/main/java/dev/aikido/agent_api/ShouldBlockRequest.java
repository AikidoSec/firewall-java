package dev.aikido.agent_api;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.ipc_commands.ShouldRateLimitCommand;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.ratelimiting.ShouldRateLimit;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;

import java.util.List;
import java.util.Optional;

import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;
import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static dev.aikido.agent_api.ratelimiting.RateLimitedEndpointFinder.getRateLimitedEndpoint;

public final class ShouldBlockRequest {
    private ShouldBlockRequest() {}
    public record ShouldBlockRequestResult(boolean block, BlockedRequestResult data) {}
    public record BlockedRequestResult(String type, String trigger, String ip) {}
    public static ShouldBlockRequestResult shouldBlockRequest() {
        ContextObject context = Context.get();
        ThreadCacheObject threadCache = ThreadCache.get();
        if (context == null || threadCache == null) {
            return new ShouldBlockRequestResult(false, null); // Blocking false
        }
        context.setExecutedMiddleware(true); // Mark middleware as executed.
        Context.set(context);
        if (context.getUser() != null) {
            if (threadCache.isBlockedUserID(context.getUser().id())) {
                return new ShouldBlockRequestResult(/*block*/ true, new BlockedRequestResult(
                        /*type*/ "blocked",/*trigger*/ "user", context.getRemoteAddress()
                ));
            }
        }

        // Get matched endpoints:
        List<Endpoint> matches = matchEndpoints(context.getRouteMetadata(), threadCache.getEndpoints());

        // Rate-limiting :
        if (matches != null && getRateLimitedEndpoint(matches, context.getRoute()) != null) {
            // As an optimization check if the route is rate limited before sending over IPC
            ThreadIPCClient threadClient = getDefaultThreadIPCClient();
            if (threadClient == null) {
                return new ShouldBlockRequestResult(false, null); // Blocking false
            }
            ShouldRateLimitCommand.Req shouldRateLimitReq = new ShouldRateLimitCommand.Req(
                    context.getRouteMetadata(), context.getUser(), context.getRemoteAddress()
            );
            Optional<ShouldRateLimit.RateLimitDecision> res =
                    new ShouldRateLimitCommand().send(threadClient, shouldRateLimitReq);
            if (res.isPresent()) {
                ShouldRateLimit.RateLimitDecision rateLimitDecision = res.get();
                if(rateLimitDecision.block()) {
                    BlockedRequestResult blockedRequestResult = new BlockedRequestResult(
                            "ratelimited", rateLimitDecision.trigger(), context.getRemoteAddress()
                    );
                    return new ShouldBlockRequestResult(true, blockedRequestResult);
                }
            }
        }

        return new ShouldBlockRequestResult(false, null); // Blocking false
    }
}
