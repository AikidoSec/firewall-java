package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.net.URL;

import static dev.aikido.agent_api.helpers.url.PortParser.getPortFromURL;

public final class URLCollector {
    private static final Logger logger = LogManager.getLogger(URLCollector.class);

    private URLCollector() {}
    public static void report(URL url) {
        if(url != null) {
            if (!url.getProtocol().startsWith("http")) {
                return; // Non-HTTP(S) URL
            }
            logger.trace("Adding a new URL to the cache: %s", url);
            int port = getPortFromURL(url);
            String hostname = url.getHost();

            // Add hostname and port to context so DNSRecordCollector can use it for SSRF detection and outbound domains
            ContextObject context = Context.get();
            if (context != null) {
                context.getHostnames().add(hostname, port);
                Context.set(context);
            }
        }
    }
}
