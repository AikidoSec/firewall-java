package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;

import java.net.URL;
import java.util.Map;

import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;
import static dev.aikido.agent_api.helpers.StackTrace.getCurrentStackTrace;
import static dev.aikido.agent_api.helpers.url.PortParser.getPortFromURL;
import static dev.aikido.agent_api.storage.AttackQueue.attackDetected;

public final class URLCollector {
    private static final Logger logger = LogManager.getLogger(URLCollector.class);

    private URLCollector() {}
    public static void report(URL url, String operation) {
        if(url != null) {
            if (!url.getProtocol().startsWith("http")) {
                return; // Non-HTTP(S) URL
            }
            logger.trace("Adding a new URL to the cache: %s", url);
            int port = getPortFromURL(url);

            // We store hostname and port in two places, HostnamesStore and Context. HostnamesStore is for reporting
            // outbound domains. Context is to have a map of hostnames with used port numbers to detect SSRF attacks.

            // hostname blocking :
            String hostname = url.getHost();
            if (ServiceConfigStore.shouldBlockOutgoingRequest(hostname)) {
                ContextObject ctx = Context.get();

                User currentUser = null;
                if (ctx != null) {
                    currentUser = ctx.getUser();
                }

                Attack attack = new Attack(
                    operation,
                    Vulnerabilities.SSRF,
                    "",
                    "",
                    Map.of(),
                    /* payload */ hostname,
                    getCurrentStackTrace(),
                    currentUser
                );

                attackDetected(attack, ctx);
                if (shouldBlock()) {
                    logger.debug("Blocking request to domain: %s", hostname);
                    throw SSRFException.get();
                }
            };

            // Store (new) hostname hits
            HostnamesStore.incrementHits(hostname, port);

            // Add to context :
            ContextObject context = Context.get();
            if (context != null) {
                context.getHostnames().add(hostname, port);
                Context.set(context);
            }
        }
    }
}
