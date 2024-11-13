package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.StringsFromContext;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.aikido.agent_api.helpers.url.UrlParser.tryParseUrl;

public final class FindHostnameInContext {
    private FindHostnameInContext() {}
    public record Res(String source, String pathToPayload, String payload) {}
    public static Res findHostnameInContext(String hostname, ContextObject context, int port) {
        Map<String, Map<String, String>> stringsFromContext = new StringsFromContext(context).getAll();
        for (Map.Entry<String, Map<String, String>> sourceEntry : stringsFromContext.entrySet()) {
            String source = sourceEntry.getKey();
            for (Map.Entry<String, String> entry : sourceEntry.getValue().entrySet()) {
                // Extract values :
                String userInput = entry.getKey();
                String path = entry.getValue();
                if (hostnameInUserInput(userInput, hostname, port)) {
                    return new Res(source, path, userInput);
                }
            }
        }
        return null;
    }
    public static boolean hostnameInUserInput(String userInput, String hostname, int port) {
        if(userInput.length() <= 1) {
            return false;
        }
        URI hostnameUrl = tryParseUrl("http://" + hostname);
        if (hostnameUrl == null) {
            return false;
        }
        List<String> variants = List.of(userInput, "http://" + userInput, "https://" + userInput);
        for(String variant: variants) {
            URI userInputUrl = tryParseUrl(variant);
            if (userInputUrl == null || userInputUrl.getHost() == null) {
                continue;
            }
            if (userInputUrl.getHost().equals(hostnameUrl.getHost())) {
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
}
