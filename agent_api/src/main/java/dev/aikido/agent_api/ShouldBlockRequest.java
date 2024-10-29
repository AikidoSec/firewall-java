package dev.aikido.agent_api;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;

public class ShouldBlockRequest {
    public record ShouldBlockRequestResult(boolean block, BlockedRequestResult data) {}
    public record BlockedRequestResult(String type, String trigger, String ip) {}
    public static ShouldBlockRequestResult shouldBlockRequest() {
        ContextObject context = Context.get();
        if (context == null) {
            return new ShouldBlockRequestResult(false, null); // Blocking false
        }
        return null;
    }
}
