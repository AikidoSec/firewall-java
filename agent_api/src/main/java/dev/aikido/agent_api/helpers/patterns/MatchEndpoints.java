package dev.aikido.agent_api.helpers.patterns;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.RouteMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromPath;
import static dev.aikido.agent_api.helpers.url.UrlParser.hasDotSegments;
import static dev.aikido.agent_api.helpers.url.UrlParser.resolveDotSegments;
import static dev.aikido.agent_api.helpers.url.UrlParser.tryParseUrlPath;

public final class MatchEndpoints {
    private MatchEndpoints() {}

    /**
     * Returns the endpoints that apply to this request. A path with dot segments can be served in two ways:
     * frameworks match it literally (/files/../admin is handled by /files/**), servlet containers resolve it
     * first (/files/../admin goes to the servlet mapped on /admin). We run before routing and can't tell which
     * one will happen, so the endpoints for both readings are returned. Callers that relax protection based
     * on the result have to deal with that ambiguity themselves, see SkipVulnerabilityScanDecider.
     */
    public static List<Endpoint> matchEndpoints(RouteMetadata routeMetadata, List<Endpoint> endpoints) {
        if (routeMetadata == null || routeMetadata.method() == null || routeMetadata.url() == null || routeMetadata.route() == null || endpoints == null) {
            return null;
        }

        // First filter based on method:
        List<Endpoint> exactMethodMatches = new ArrayList<>();
        List<Endpoint> possible = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            if (endpoint.getMethod().equals(routeMetadata.method())) {
                exactMethodMatches.add(endpoint);
            } else if ("*".equals(endpoint.getMethod())) {
                possible.add(endpoint);
            }
        }
        // Make sure the exact matches come first:
        possible.addAll(0, exactMethodMatches);

        // Parse URL:
        String path = tryParseUrlPath(routeMetadata.url());
        if (path == null) {
            return null;
        }

        List<Endpoint> results = matchRouteAndPath(possible, routeMetadata.route(), path);
        if (hasDotSegments(path)) {
            String resolvedPath = resolveDotSegments(path);
            for (Endpoint endpoint : matchRouteAndPath(possible, buildRouteFromPath(resolvedPath), resolvedPath)) {
                if (!results.contains(endpoint)) {
                    results.add(endpoint);
                }
            }
        }

        return results.isEmpty() ? null : results;
    }

    private static List<Endpoint> matchRouteAndPath(List<Endpoint> possible, String route, String path) {
        // If routes match exact add to results
        List<Endpoint> results = new ArrayList<>();
        for (Endpoint endpoint : possible) {
            if (endpoint.getRoute().equals(route)) {
                results.add(endpoint);
            }
        }

        List<Endpoint> wildcards = new ArrayList<>();
        for (Endpoint endpoint : possible) {
            if (endpoint.getRoute().contains("*")) {
                wildcards.add(endpoint);
            }
        }

        wildcards.sort((e1, e2) -> Integer.compare(e2.getRoute().split("\\*").length - 1, e1.getRoute().split("\\*").length - 1));

        for (Endpoint wildcard : wildcards) {
            String regexString = "^" + wildcard.getRoute().replace("*", "(.*)") + "/?$";
            Pattern regex = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);

            if (regex.matcher(path).matches()) {
                results.add(wildcard);
            }
        }

        return results;
    }
}
