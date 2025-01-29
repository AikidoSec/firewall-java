package helpers;

import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.RouteMetadata;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MatchEndpointsTest {

    private RouteMetadata sampleRouteMetadata(String url, String method, String route) {
        if (route == null) {
            route = "/posts/:number";
        }
        if (method == null) {
            method = "POST";
        }
        if (url == null) {
            url = "http://localhost:4000/posts/3";
        }
        return new RouteMetadata(route, url, method);
    }

    @Test
    void testInvalidUrlAndNoRoute() {
        assertNull(matchEndpoints(sampleRouteMetadata(null, null, "abc"), Collections.emptyList()));
    }

    @Test
    void testNoUrlAndNoRoute() {
        assertNull(matchEndpoints(new RouteMetadata(null, null, "GET"), Collections.emptyList()));
    }

    @Test
    void testNoMethod() {
        assertNull(matchEndpoints(
                new RouteMetadata("/posts/:id", "http://localhost:4000/posts/3", null), Collections.emptyList()));
    }

    @Test
    void testItReturnsUndefinedIfNothingFound() {
        assertNull(matchEndpoints(sampleRouteMetadata(null, null, null), Collections.emptyList()));
    }

    @Test
    void testItReturnsEndpointBasedOnRoute() {
        List<Endpoint> endpoints = List.of(new Endpoint(
                "POST", "/posts/:number", 10 /*maxRequests*/, 1000 /*windowSizeInMS*/, List.of(), false, false, true));
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata(null, null, null), endpoints));
    }

    @Test
    void testItReturnsEndpointBasedOnRelativeUrl() {
        List<Endpoint> endpoints = List.of(new Endpoint(
                "POST", "/posts/:number", 10 /*maxRequests*/, 1000 /*windowSizeInMS*/, List.of(), false, false, true));
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata("/posts/3", "POST", "/posts/:number"), endpoints));
    }

    @Test
    void testItReturnsEndpointBasedOnWildcard() {
        List<Endpoint> endpoints = List.of(new Endpoint(
                "*", "/posts/*", 10 /*maxRequests*/, 1000 /*windowSizeMS*/, List.of(), false, false, true));
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata(null, null, null), endpoints));
    }

    @Test
    void testItReturnsEndpointBasedOnWildcardWithRelativeUrl() {
        List<Endpoint> endpoints = List.of(new Endpoint(
                "*", "/posts/*", 10 /*maxRequests*/, 1000 /*windowSizeMs*/, List.of(), false, false, true));
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata(null, null, "/posts/3"), endpoints));
    }

    @Test
    void testItFavorsMoreSpecificWildcard() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("*", "/posts/*", 10, 1000, List.of(), false, false, true),
                new Endpoint("*", "/posts/*/comments/*", 10, 1000, List.of(), false, false, true));

        List<Endpoint> expected = List.of(endpoints.get(1), endpoints.get(0));
        assertEquals(
                expected,
                matchEndpoints(
                        sampleRouteMetadata("http://localhost:4000/posts/3/comments/10", null, null), endpoints));
    }

    @Test
    void testItMatchesWildcardRouteWithSpecificMethod() {
        List<Endpoint> endpoints =
                List.of(new Endpoint("POST", "/posts/*/comments/*", 10, 1000, List.of(), false, false, true));
        assertEquals(
                endpoints,
                matchEndpoints(
                        sampleRouteMetadata("http://localhost:4000/posts/3/comments/10", null, null), endpoints));
    }

    @Test
    void testItPrefersSpecificRouteOverWildcard() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("*", "/api/*", 20, 60000, List.of(), false, false, true),
                new Endpoint("POST", "/api/coach", 100, 60000, List.of(), false, false, true));

        List<Endpoint> expected = List.of(endpoints.get(1), endpoints.get(0));

        assertEquals(
                expected,
                matchEndpoints(sampleRouteMetadata("http://localhost:4000/api/coach", null, "/api/coach"), endpoints));
    }

    @Test
    void testItPrefersSpecificMethodOverWildcardFirstCase() {
        RouteMetadata routeMetadata = sampleRouteMetadata("http://localhost:4000/api/test", "POST", "/api/test");

        List<Endpoint> endpoints = List.of(
                new Endpoint("*", "/api/test", 20, 60000, List.of(), false, false, true),
                new Endpoint("POST", "/api/test", 100, 60000, List.of(), false, false, true));

        List<Endpoint> expected = List.of(endpoints.get(1), endpoints.get(0));
        assertEquals(expected, matchEndpoints(routeMetadata, endpoints));
    }

    @Test
    void testItPrefersSpecificMethodOverWildcardSecondCase() {
        RouteMetadata routeMetadata = sampleRouteMetadata("http://localhost:4000/api/test", "POST", "/api/test");

        List<Endpoint> endpoints = List.of(
                new Endpoint("POST", "/api/test", 100, 60000, List.of(), false, false, true),
                new Endpoint("*", "/api/test", 20, 60000, List.of(), false, false, true));

        List<Endpoint> expected = List.of(endpoints.get(0), endpoints.get(1));
        assertEquals(expected, matchEndpoints(routeMetadata, endpoints));
    }
}
