package com.example.probe;

import dev.aikido.agent_api.ShouldBlockRequest;
import io.micronaut.core.annotation.Order;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.exceptions.HttpStatusException;

import static io.micronaut.http.annotation.ServerFilter.MATCH_ALL_PATTERN;

/**
 * Rate-limiting and user-blocking, mirroring AikidoJavalinMiddleware / Spring's RateLimitingFilter.
 * Ordered after SetUserFilter so the user is available; runs on every request before the controller.
 */
@ServerFilter(MATCH_ALL_PATTERN)
@Order(100)
public class AikidoRateLimitFilter {
    @RequestFilter
    public void onRequest() {
        ShouldBlockRequest.ShouldBlockRequestResult result = ShouldBlockRequest.shouldBlockRequest();
        if (!result.block()) {
            return;
        }
        int status;
        String message;
        if ("ratelimited".equals(result.data().type())) {
            status = 429;
            message = "You are rate limited by Zen.";
            if ("ip".equals(result.data().trigger()) && result.data().ip() != null) {
                message += " (Your IP: " + result.data().ip() + ")";
            }
        } else {
            status = 403;
            message = "You are blocked by Zen.";
        }
        throw new HttpStatusException(HttpStatus.valueOf(status), (Object) message);
    }
}
