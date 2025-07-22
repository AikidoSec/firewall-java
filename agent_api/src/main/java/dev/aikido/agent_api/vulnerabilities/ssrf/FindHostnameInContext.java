package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.StringsFromContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.aikido.agent_api.helpers.url.UrlParser.tryParseUrl;
import static dev.aikido.agent_api.vulnerabilities.ssrf.RequestToItselfChecker.isRequestToItself;
import static dev.aikido.agent_api.vulnerabilities.ssrf.RequestToServiceHostnameChecker.isRequestToServiceHostname;

public final class FindHostnameInContext {
    private FindHostnameInContext() {}
    public record Res(String source, String pathToPayload, String payload) {}

    public static Res findHostnameInContext(String hostname, ContextObject context, int port) {
        // We don't want to block outgoing requests to the same host as the server
        // (often happens that we have a match on headers like `Host`, `Origin`, `Referer`, etc.)
        if (isRequestToItself(context.getUrl(), hostname, port)) {
            return null;
        }

        // We don't want to block outgoing requests where the hostname is a service name, even if it's inside user input
        if (isRequestToServiceHostname(hostname)) {
            return null;
        }

        // get hostname options
        List<String> hostnameOptions = getHostnameOptions(hostname);

        Map<String, Map<String, String>> stringsFromContext = new StringsFromContext(context).getAll();
        for (Map.Entry<String, Map<String, String>> sourceEntry : stringsFromContext.entrySet()) {
            String source = sourceEntry.getKey();
            for (Map.Entry<String, String> entry : sourceEntry.getValue().entrySet()) {
                // Extract values :
                String userInput = entry.getKey();
                String path = entry.getValue();
                if (hostnameInUserInput(userInput, hostnameOptions, port)) {
                    return new Res(source, path, userInput);
                }
            }
        }
        return null;
    }
    public static boolean hostnameInUserInput(String userInput, List<String> hostnameOptions, int port) {
        if(userInput.length() <= 1) {
            return false;
        }

        List<String> variants = List.of(userInput, "http://" + userInput, "https://" + userInput);
        for(String variant: variants) {
            URI userInputUrl = tryParseUrl(variant);
            if (userInputUrl == null || userInputUrl.getHost() == null) {
                continue;
            }

            // the hostname options include different options for what the normalized hostname could be
            // this is useful to also try to parse ::1 as http://[::1]/ (otherwise it would fail)
            if (hostnameOptions.contains(userInputUrl.getHost().toLowerCase())) {
                if (userInputUrl.getPort() == -1 || port == -1) {
                    return true;
                }
                if (userInputUrl.getPort() == port) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getHostnameOptions(String hostname) {
        List<URI> uriOptions = new ArrayList<>();

        // basic case to normalize hostname
        uriOptions.add(tryParseUrl(String.format("http://%s", hostname)));

        // Add a case for hostnames like ::1 or ::ffff:127.0.0.1, who need brackets to be parsed
        uriOptions.add(tryParseUrl(String.format("http://[%s]", hostname)));

        List<String> options = new ArrayList<>();
        for (URI optionsUri : uriOptions) {
            if (optionsUri != null && optionsUri.getHost() != null) {
                // make sure to lowercase, so the match works
                options.add(optionsUri.getHost().toLowerCase());
            }
        }

        return options;
    }
}
