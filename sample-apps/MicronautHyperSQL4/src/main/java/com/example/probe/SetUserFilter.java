package com.example.probe;

import dev.aikido.agent_api.SetUser;
import io.micronaut.core.annotation.Order;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;

import static io.micronaut.http.annotation.ServerFilter.MATCH_ALL_PATTERN;

/** Mirrors the Javalin/Spring sample apps' SetUserHandler/SetUserFilter: useful for end-2-end tests. */
@ServerFilter(MATCH_ALL_PATTERN)
@Order(10) // must run before AikidoRateLimitFilter so the user is set
public class SetUserFilter {
    @RequestFilter
    public void onRequest(HttpRequest<?> request) {
        String userId = request.getHeaders().get("user");
        if (userId != null) {
            SetUser.setUser(new SetUser.UserObject(userId, "John Doe"));
        }
    }
}
