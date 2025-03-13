package helpers;

import dev.aikido.agent_api.helpers.url.IsUsefulRoute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IsUsefulRouteTest {

    @Test
    void testNotFoundOrMethodNotAllowed() {
        assertFalse(IsUsefulRoute.isUsefulRoute(404, "/", "GET"));
        assertFalse(IsUsefulRoute.isUsefulRoute(405, "/", "GET"));
    }

    @Test
    void testDiscoverRouteForAllOtherStatusCodes() {
        assertTrue(IsUsefulRoute.isUsefulRoute(200, "/", "GET"));
        assertFalse(IsUsefulRoute.isUsefulRoute(500, "/", "GET"));
        assertFalse(IsUsefulRoute.isUsefulRoute(400, "/", "GET"));
        assertFalse(IsUsefulRoute.isUsefulRoute(199, "/", "GET"));
        assertTrue(IsUsefulRoute.isUsefulRoute(399, "/", "GET"));
        assertTrue(IsUsefulRoute.isUsefulRoute(300, "/", "GET"));
        assertTrue(IsUsefulRoute.isUsefulRoute(201, "/", "GET"));
    }

    @Test
    void testNotDiscoverRouteForExcludedMethods() {
        assertFalse(IsUsefulRoute.isUsefulRoute(200, "/", "OPTIONS"));
        assertFalse(IsUsefulRoute.isUsefulRoute(200, "/", "HEAD"));
    }

    @Test
    void testNotDiscoverRouteForExcludedMethodsWithOtherStatusCodes() {
        assertFalse(IsUsefulRoute.isUsefulRoute(404, "/", "OPTIONS"));
        assertFalse(IsUsefulRoute.isUsefulRoute(405, "/", "HEAD"));
    }

    @Test
    void testNotDiscoverStaticFiles() {
        assertFalse(IsUsefulRoute.isUsefulRoute(200, "/service-worker.js", "GET"));
        assertFalse(IsUsefulRoute.isUsefulRoute(200, "/precache-manifest.10faec0bee24db502c8498078126dd53.js", "POST"));
        assertFalse(IsUsefulRoute.isUsefulRoute(200, "/img/icons/favicon-16x16.png", "GET"));
        assertFalse(IsUsefulRoute.isUsefulRoute(200, "/fonts/icomoon.ttf", "GET"));
    }

    @Test
    void testWellKnownDirectory() {
        assertFalse(IsUsefulRoute.isUsefulRoute(200, "/.well-known", "GET"));
        assertTrue(IsUsefulRoute.isUsefulRoute(200, "/.well-known/change-password", "GET"));
        assertFalse(IsUsefulRoute.isUsefulRoute(200, "/.well-known/security.txt", "GET"));
    }
}
