package dev.aikido.agent_api.helpers.patterns;

import static dev.aikido.agent_api.helpers.url.UrlParser.tryParseUrlPath;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.RouteMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class MatchEndpoints {
    private MatchEndpoints() {}

    public static List<Endpoint> matchEndpoints(RouteMetadata routeMetadata, List<Endpoint> endpoints) {
        if (routeMetadata == null
                || routeMetadata.method() == null
                || routeMetadata.url() == null
                || routeMetadata.route() == null
                || endpoints == null) {
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

        // If routes match exact add to results
        List<Endpoint> results = new ArrayList<>();
        for (Endpoint endpoint : possible) {
            if (endpoint.getRoute().equals(routeMetadata.route())) {
                results.add(endpoint);
            }
        }

        // Parse URL:
        if (routeMetadata.url() == null || routeMetadata.url().isEmpty()) {
            return null;
        }
        String path = tryParseUrlPath(routeMetadata.url());
        if (path == null) {
            return null;
        }

        List<Endpoint> wildcards = new ArrayList<>();
        for (Endpoint endpoint : possible) {
            if (endpoint.getRoute().contains("*")) {
                wildcards.add(endpoint);
            }
        }

        wildcards.sort((e1, e2) -> Integer.compare(
                e2.getRoute().split("\\*").length - 1, e1.getRoute().split("\\*").length - 1));

        for (Endpoint wildcard : wildcards) {
            String route = wildcard.getRoute();
            String regexString = "^" + route.replace("*", "(.*)") + "/?$";
            Pattern regex = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);

            if (regex.matcher(path).matches()) {
                results.add(wildcard);
            }
        }

        return results.isEmpty() ? null : results;
    }
}
