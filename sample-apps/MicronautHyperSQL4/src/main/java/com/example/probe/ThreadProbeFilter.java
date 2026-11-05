package com.example.probe;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;

import java.util.UUID;

import static io.micronaut.http.annotation.ServerFilter.MATCH_ALL_PATTERN;

/**
 * Simulates where Zen would set the request context / run inbound blocking (IP/UA).
 * We stamp a correlation id and log which thread the filter runs on.
 */
@ServerFilter(MATCH_ALL_PATTERN)
public class ThreadProbeFilter {

    @RequestFilter
    public void onRequest(HttpRequest<?> request) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        request.getAttributes().put("probeId", id);
        ProbeContext.CTX.set(id); // Zen sets its context here, at inbound entry
        ThreadLog.log(id, "1-server-filter (inbound entry)");
    }
}
