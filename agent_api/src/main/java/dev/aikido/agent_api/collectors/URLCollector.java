package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.PendingHostnamesStore;

import java.net.URL;

import static dev.aikido.agent_api.helpers.url.PortParser.getPortFromURL;

public final class URLCollector {
    private static final Logger logger = LogManager.getLogger(URLCollector.class);

    private URLCollector() {}
    public static void report(URL url) {
        report(url, Context.get());
    }

    // Used where the caller already resolved the correct context itself instead of relying on
    // Context.get() (e.g. Spring WebClient reading it back from Reactor's own Context, which
    // survives scheduler hops that break Context.get()'s ThreadLocal).
    public static void report(URL url, ContextObject context) {
        if (url != null) {
            if (!url.getProtocol().startsWith("http")) {
                return; // Non-HTTP(S) URL
            }
            logger.trace("Adding a new URL to the cache: %s", url);
            // Store hostname+port in the pending store so DNSRecordCollector can pick it
            // up during the DNS lookup that follows, for SSRF detection and outbound hostnames
            PendingHostnamesStore.add(url.getHost(), getPortFromURL(url), context);
        }
    }
}
