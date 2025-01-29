package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;

import java.net.URL;

import static dev.aikido.agent_api.helpers.url.PortParser.getPortFromURL;
import static dev.aikido.agent_api.vulnerabilities.ssrf.FindHostnameInContext.findHostnameInContext;
import static dev.aikido.agent_api.vulnerabilities.ssrf.RedirectOriginFinder.getRedirectOrigin;

public final class PrivateIPRedirectFinder {
    private PrivateIPRedirectFinder() {
    }

    /**
     * This function is called before a outgoing request is made.
     * It's used to prevent requests to private IP addresses after a redirect with a user-supplied URL (SSRF).
     * It returns true if the following conditions are met:
     * - context.outgoingRequestRedirects is set: Inside the context of this incoming request, there was a redirect
     * - The hostname of the URL contains a private IP address
     * - The redirect origin, so the user-supplied hostname and port that caused the first redirect, is found in the context of the incoming request
     */
    public static FindHostnameInContext.Res isRedirectToPrivateIP(String hostname, int port) {
        ContextObject context = Context.get();
        if (!context.getRedirectStartNodes().isEmpty()) {
            URL redirectOrigin = getRedirectOrigin(hostname, port);
            if (redirectOrigin != null) {
                String originHostname = redirectOrigin.getHost();
                int originPort = getPortFromURL(redirectOrigin);
                return findHostnameInContext(originHostname, context, originPort);
            }
        }
        return null;
    }
}
