package dev.aikido.agent_api;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.utilities.IPCClient;
import dev.aikido.agent_api.background.utilities.IPCDefaultClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;

import java.util.List;
import java.util.Optional;

import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static dev.aikido.agent_api.ratelimiting.RateLimitedEndpointFinder.getRateLimitedEndpoint;

public class ShouldBlockRequest {
    public record ShouldBlockRequestResult(boolean block, BlockedRequestResult data) {}
    public record BlockedRequestResult(String type, String trigger, String ip) {}
    public static ShouldBlockRequestResult shouldBlockRequest() {
        ContextObject context = Context.get();
        ThreadCacheObject threadCache = ThreadCache.get();
        if (context == null || threadCache == null) {
            return new ShouldBlockRequestResult(false, null); // Blocking false
        }

        // Check for blocked users after that PR here.

        // Get matched endpoints:
        List<Endpoint> matches = matchEndpoints(context.getRouteMetadata(), threadCache.getEndpoints());

        // Rate-limiting :
        if (matches != null && getRateLimitedEndpoint(matches, context.getRoute()) != null) {
            // As an optimization check if the route is rate limited before sending over IPC
            IPCClient ipcClient = new IPCDefaultClient();
            String jsonDataPacket = "";
            Optional<String> res = ipcClient.sendData("SHOULD_RATELIMIT$" + jsonDataPacket, true);
        }

        return new ShouldBlockRequestResult(false, null); // Blocking false
    }
}
