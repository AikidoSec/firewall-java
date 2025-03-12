package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.HostnamesStore;

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

            // We store hostname and port in two places, HostnamesStore and Context. HostnamesStore is for reporting
            // outbound domains. Context is to have a map of hostnames with used port numbers to detect SSRF attacks.

            // Store (new) hostname hits
            HostnamesStore.incrementHits(url.getHost(), port);

            // Add to context :
            ContextObject context = Context.get();
            if (context != null) {
                context.getHostnames().add(url.getHost(), port);
                Context.set(context);
            }
        }
    }
}
