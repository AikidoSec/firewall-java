package dev.aikido;

import dev.aikido.agent_api.ShouldBlockRequest;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class RateLimitingMiddleware implements Handler {

    @Override
    public void handle(Context ctx) throws Exception {
        ShouldBlockRequest.ShouldBlockRequestResult shouldBlockRequestResult = ShouldBlockRequest.shouldBlockRequest();

        if (shouldBlockRequestResult.block()) {
            if (shouldBlockRequestResult.data().type().equals("ratelimited")) {
                String message = "You are rate limited by Zen.";
                if (shouldBlockRequestResult.data().trigger().equals("ip")) {
                    message += " (Your IP: " + shouldBlockRequestResult.data().ip() + ")";
                }
                setResponse(ctx, message, 429);
            } else if (shouldBlockRequestResult.data().type().equals("blocked")) {
                setResponse(ctx, "You are blocked by Zen.", 403);
            }
            return; // Stop further processing
        }
    }

    private void setResponse(Context ctx, String text, int statusCode) {
        ctx.status(statusCode);
        ctx.contentType("text/plain");
        ctx.result(text);
    }
}