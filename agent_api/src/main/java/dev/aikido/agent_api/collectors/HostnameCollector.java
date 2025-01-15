package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.background.ipc_commands.AttackCommand;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFDetector;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;
import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;

public final class HostnameCollector {
    private HostnameCollector() {}
    private static final Logger logger = LogManager.getLogger(HostnameCollector.class);
    public static void report(String hostname, InetAddress[] inetAddresses) {
        try {
            logger.trace("HostnameCollector called with %s & inet addresses: %s", hostname, List.of(inetAddresses));


            // Convert inetAddresses array to a List of IP strings :
            List<String> ipAddresses = new ArrayList<>();
            for (InetAddress inetAddress : inetAddresses) {
                ipAddresses.add(inetAddress.getHostAddress());
            }
            // Currently using hostnames from thread cache, might not be as accurate as using Context-dependant hostnames.
            if (ThreadCache.get() == null || ThreadCache.get().getHostnames() == null) {
                logger.trace("Thread cache is empty, returning.");
                return;
            }
            for (Hostnames.HostnameEntry hostnameEntry : ThreadCache.get().getHostnames().asArray()) {
                if (!hostnameEntry.getHostname().equals(hostname)) {
                    continue;
                }
                logger.debug("Hostname: %s, Port: %s, IPs: %s", hostnameEntry.getHostname(), hostnameEntry.getPort(), ipAddresses);

                Attack attack = new SSRFDetector().run(
                        hostname, hostnameEntry.getPort(), ipAddresses,
                        /* operation: */ "java.net.InetAddress.getAllByName");
                if (attack == null) {
                    continue;
                }
                logger.debug("SSRF Attack detected due to: %s:%s", hostname, hostnameEntry.getPort());

                ThreadIPCClient client = getDefaultThreadIPCClient();
                AttackCommand.Req req = new AttackCommand.Req(attack, Context.get());
                if (client != null) {
                    AttackCommand.sendAttack(client, req);
                }

                if (shouldBlock()) {
                    logger.debug("Blocking SSRF attack...");
                    throw SSRFException.get();
                }
            }
        } catch (SSRFException e) {
            throw e;
        } catch (Throwable e) {
            logger.trace(e);
        }
    }
}
