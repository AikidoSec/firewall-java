package helpers;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.RouteMetadata;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MatchEndpointsTest {

    private RouteMetadata sampleRouteMetadata(String url, String method, String route) {
        if (route == null) {
            route = "/posts/:number";
        } if(method == null) {
            method = "POST";
        } if(url == null) {
            url = "http://localhost:4000/posts/3";
        }
        return new RouteMetadata(route, url, method);
    }

    @Test
    void testInvalidUrlAndNoRoute() {
        assertNull(matchEndpoints(new RouteMetadata(null, "abc", null), Collections.emptyList()));
    }

    @Test
    void testNoUrlAndNoRoute() {
        assertNull(matchEndpoints(new RouteMetadata(null, null, "GET"), Collections.emptyList()));
    }

    @Test
    void testNoMethod() {
        assertNull(matchEndpoints(new RouteMetadata("/posts/:id", "http://localhost:4000/posts/3", null), Collections.emptyList()));
    }

    @Test
    void testItReturnsUndefinedIfNothingFound() {
        assertNull(matchEndpoints(sampleRouteMetadata(null, null, null), Collections.emptyList()));
    }

    @Test
    void testItReturnsEndpointBasedOnRoute() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("POST", "/posts/:number",
                        10 /*maxRequests*/, 1000 /*windowSizeInMS*/,
                        List.of(), false, false, true));
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata(null, null, null), endpoints));
    }

    @Test
    void testItReturnsEndpointBasedOnRelativeUrl() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("POST", "/posts/:number",
                        10 /*maxRequests*/, 1000 /*windowSizeInMS*/,
                        List.of(), false, false, true));
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata("/posts/3", "POST", "/posts/:number"), endpoints));
    }

    @Test
    void testItReturnsEndpointBasedOnWildcard() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("*", "/posts/*",
                        10 /*maxRequests*/, 1000 /*windowSizeMS*/,
                        List.of(), false, false, true));
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata(null, null, null), endpoints));
    }
    @Test
    void testItReturnsEndpointBasedOnWildcardWithRelativeUrl() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("*", "/posts/*",
                        10 /*maxRequests*/, 1000 /*windowSizeMs*/,
                        List.of(), false, false, true));
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata(null, null, "/posts/3"), endpoints));
    }
    @Test
    void testItFavorsMoreSpecificWildcard() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("*", "/posts/*", 10, 1000, List.of(), false, false, true),
                new Endpoint("*", "/posts/*/comments/*", 10, 1000, List.of(), false, false, true)
        );

        List<Endpoint> expected = List.of(
                endpoints.get(1),
                endpoints.get(0)
        );
        assertEquals(expected, matchEndpoints(sampleRouteMetadata("http://localhost:4000/posts/3/comments/10", null, null), endpoints));
    }

    @Test
    void testItMatchesWildcardRouteWithSpecificMethod() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("POST", "/posts/*/comments/*", 10, 1000, List.of(), false, false, true)
        );
        assertEquals(endpoints, matchEndpoints(sampleRouteMetadata("http://localhost:4000/posts/3/comments/10", null, null), endpoints));
    }
    @Test
    void testItPrefersSpecificRouteOverWildcard() {
        List<Endpoint> endpoints = List.of(
                new Endpoint("*", "/api/*", 20, 60000, List.of(), false, false, true),
                new Endpoint("POST", "/api/coach", 100, 60000, List.of(), false, false, true)
        );

        List<Endpoint> expected = List.of(
                endpoints.get(1),
                endpoints.get(0)
        );

        assertEquals(expected, matchEndpoints(sampleRouteMetadata("http://localhost:4000/api/coach", null, "/api/coach"), endpoints));
    }

    @Test
    void testItPrefersSpecificMethodOverWildcardFirstCase() {
        RouteMetadata routeMetadata = sampleRouteMetadata(
                "http://localhost:4000/api/test", "POST", "/api/test"
        );

        List<Endpoint> endpoints = List.of(
                new Endpoint("*", "/api/test", 20, 60000, List.of(), false, false, true),
                new Endpoint("POST", "/api/test", 100, 60000, List.of(), false, false, true)
        );

        List<Endpoint> expected = List.of(
                endpoints.get(1),
                endpoints.get(0)
        );
        assertEquals(expected, matchEndpoints(routeMetadata, endpoints));
    }

    @Test
    void testItPrefersSpecificMethodOverWildcardSecondCase() {
        RouteMetadata routeMetadata = sampleRouteMetadata(
                "http://localhost:4000/api/test", "POST", "/api/test"
        );

        List<Endpoint> endpoints = List.of(
                new Endpoint("POST", "/api/test", 100, 60000, List.of(), false, false, true),
                new Endpoint("*", "/api/test", 20, 60000, List.of(), false, false, true)
        );

        List<Endpoint> expected = List.of(
                endpoints.get(0),
                endpoints.get(1)
        );
        assertEquals(expected, matchEndpoints(routeMetadata, endpoints));
    }

    private List<Endpoint> matchUrl(String url, List<Endpoint> endpoints) {
        // Same as the context objects: the route is derived from the request URL.
        return matchEndpoints(new RouteMetadata(buildRouteFromUrl(url), url, "GET"), endpoints);
    }

    @Test
    void testAlternativeSpellingsMatchTheSameEndpoints() {
        Endpoint adminWildcard = new Endpoint("GET", "/admin/*", 3, 60000, List.of(), false, false, true);
        Endpoint adminDelete = new Endpoint("GET", "/admin/delete", 3, 60000, List.of(), false, false, true);
        Endpoint publicWildcard = new Endpoint("GET", "/public/*", 100, 60000, List.of(), false, false, true);
        List<Endpoint> endpoints = List.of(publicWildcard, adminWildcard, adminDelete);

        List<String> urls = List.of(
                "http://127.0.0.1:8099/admin/delete",
                "http://127.0.0.1:8099/admin;x=y/delete",
                "http://127.0.0.1:8099/admin/delete;x=y",
                "http://127.0.0.1:8099//admin//delete",
                "http://127.0.0.1:8099/%61dmin/delete",
                "http://127.0.0.1:8099/admin/delete?q=%zz",
                "http://127.0.0.1:8099/admin/delete?q=a|b"
        );
        for (String url : urls) {
            assertEquals(List.of(adminDelete, adminWildcard), matchUrl(url, endpoints), url);
        }
    }

    @Test
    void testDotSegmentsMatchBothTheLiteralAndTheResolvedPath() {
        Endpoint adminWildcard = new Endpoint("GET", "/admin/*", 3, 60000, List.of(), false, false, true);
        Endpoint adminDelete = new Endpoint("GET", "/admin/delete", 3, 60000, List.of(), false, false, true);
        Endpoint publicWildcard = new Endpoint("GET", "/public/*", 100, 60000, List.of(), false, false, true);
        List<Endpoint> endpoints = List.of(publicWildcard, adminWildcard, adminDelete);

        List<String> urls = List.of(
                "http://127.0.0.1:8099/public/../admin/delete",
                "http://127.0.0.1:8099/public/%2e%2e/admin/delete",
                "http://127.0.0.1:8099/public/..;/admin/delete",
                "http://127.0.0.1:8099/public/x/../../admin/delete"
        );
        for (String url : urls) {
            // Literal reading first (served by /public/**), then the resolved one (served by /admin/delete).
            assertEquals(List.of(publicWildcard, adminDelete, adminWildcard), matchUrl(url, endpoints), url);
        }

        // Outside of both readings:
        assertEquals(List.of(adminDelete, adminWildcard), matchUrl("http://127.0.0.1:8099/a/b/../../admin/delete", endpoints));
        assertEquals(List.of(publicWildcard), matchUrl("http://127.0.0.1:8099/public/../other", endpoints));
        assertNull(matchUrl("http://127.0.0.1:8099/a/../other", endpoints));
    }

    @Test
    void testDotSegmentsDoNotDuplicateEndpoints() {
        Endpoint adminWildcard = new Endpoint("GET", "/admin/*", 3, 60000, List.of(), false, false, true);
        assertEquals(List.of(adminWildcard), matchUrl("http://127.0.0.1:8099/admin/x/../delete", List.of(adminWildcard)));
    }
}
