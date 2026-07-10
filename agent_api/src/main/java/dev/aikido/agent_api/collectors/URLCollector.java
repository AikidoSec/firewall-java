package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.PendingHostnamesStore;

import java.net.URL;

import static dev.aikido.agent_api.helpers.url.PortParser.getPortFromURL;

public final class URLCollector {
    private static final Logger logger = LogManager.getLogger(URLCollector.class);

    private URLCollector() {}
    public static void report(URL url) {
        if (url != null) {
            if (!url.getProtocol().startsWith("http")) {
                return; // Non-HTTP(S) URL
            }
            logger.trace("Adding a new URL to the cache: %s", url);
            int port = getPortFromURL(url);
            // Store hostname+port in the pending store so DNSRecordCollector can pick it
            // up during the DNS lookup that follows, for SSRF detection
            PendingHostnamesStore.add(url.getHost(), port);
            // Record the hit here, at the real call site, where the port is always known -
            // instead of waiting for the DNS lookup, which can't distinguish this call from
            // unrelated infra noise resolving the same hostname.
            HostnamesStore.incrementHits(url.getHost(), port);
        }
    }
}
