package dev.aikido.agent_api.vulnerabilities.ssrf;

import java.net.URI;
import java.util.Objects;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.trustProxy;
import static dev.aikido.agent_api.helpers.url.UrlParser.tryParseUrl;

public final class RequestToItselfChecker {
    private RequestToItselfChecker() {
    }

    /**
     * We don't want to block outgoing requests to the same host as the server
     * (often happens that we have a match on headers like `Host`, `Origin`, `Referer`, etc.)
     * We have to check the port as well, because the hostname can be the same but with a different port
     */
    public static boolean isRequestToItself(String serverUrl, String outboundHostname, int outboundPort) {
        // When the app is not behind a reverse proxy, we can't trust the hostname inside `serverUrl`
        // The hostname in `serverUrl` is built from the request headers
        // The headers can be manipulated by the client if the app is directly exposed to the internet
        if (!trustProxy()) {
            return false;
        }

        URI baseUrl = tryParseUrl(serverUrl);
        if (baseUrl == null) {
            return false;
        }

        if (Objects.equals(baseUrl.getHost(), outboundHostname)) {
            return false; // the outbound hostname is different from the server url's hostname.
        }

        // If the port and hostname are the same, the server is making a request to itself
        if (baseUrl.getPort() == outboundPort) {
            return true;
        }

        // Special case for HTTP/HTTPS ports
        // In production, the app will be served on port 80 and 443
        if (baseUrl.getPort() == 80 && outboundPort == 443) {
            return true;
        }
        if (baseUrl.getPort() == 443 && outboundPort == 80) {
            return true;
        }

        return false;
    }
}
