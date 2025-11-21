package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFDetector;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.ssrf.StoredSSRFDetector;
import dev.aikido.agent_api.vulnerabilities.ssrf.StoredSSRFException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;
import static dev.aikido.agent_api.storage.AttackQueue.attackDetected;

public final class DNSRecordCollector {
    private DNSRecordCollector() {}
    private static final Logger logger = LogManager.getLogger(DNSRecordCollector.class);
    private static final String OPERATION_NAME = "java.net.InetAddress.getAllByName";
    public static void report(String hostname, InetAddress[] inetAddresses) {
        try {
            logger.trace("DNSRecordCollector called with %s & inet addresses: %s", hostname, List.of(inetAddresses));

            // store stats
            StatisticsStore.registerCall("java.net.InetAddress.getAllByName", OperationKind.OUTGOING_HTTP_OP);

            // Convert inetAddresses array to a List of IP strings :
            List<String> ipAddresses = new ArrayList<>();
            for (InetAddress inetAddress : inetAddresses) {
                ipAddresses.add(inetAddress.getHostAddress());
            }

            // Fetch hostnames from Context (this is to get port number e.g.)
            if (Context.get() != null && Context.get().getHostnames() != null) {
                for (Hostnames.HostnameEntry hostnameEntry : Context.get().getHostnames().asArray()) {
                    if (!hostnameEntry.getHostname().equals(hostname)) {
                        continue;
                    }
                    logger.debug("Hostname: %s, Port: %s, IPs: %s", hostnameEntry.getHostname(), hostnameEntry.getPort(), ipAddresses);

                    Attack attack = SSRFDetector.run(
                        hostname, hostnameEntry.getPort(), ipAddresses, OPERATION_NAME
                    );
                    if (attack == null) {
                        continue;
                    }

                    logger.debug("SSRF Attack detected due to: %s:%s", hostname, hostnameEntry.getPort());
                    attackDetected(attack, Context.get());

                    if (shouldBlock()) {
                        logger.debug("Blocking SSRF attack...");
                        throw SSRFException.get();
                    }

                    // We don't want to test for a stored SSRF attack.
                    return;
                }
            }

            // We don't need the context object to check for stored ssrf, but we do want to run this after our other
            // SSRF checks, making sure if it's a normal ssrf attack it gets reported like that.
            Attack storedSsrfAttack = new StoredSSRFDetector().run(hostname, ipAddresses, OPERATION_NAME);
            if (storedSsrfAttack != null) {
                attackDetected(storedSsrfAttack, Context.get());

                if (shouldBlock()) {
                    logger.debug("Blocking stored SSRF attack...");
                    throw StoredSSRFException.get();
                }
            }

        } catch (SSRFException | StoredSSRFException e) {
            throw e;
        } catch (Throwable e) {
            logger.trace(e);
        }
    }
}
