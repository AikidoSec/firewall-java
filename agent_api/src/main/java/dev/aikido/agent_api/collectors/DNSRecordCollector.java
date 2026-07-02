package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.PendingHostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFDetector;
import dev.aikido.agent_api.vulnerabilities.outbound_blocking.BlockedOutboundException;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP;
import dev.aikido.agent_api.vulnerabilities.ssrf.StoredSSRFDetector;
import dev.aikido.agent_api.vulnerabilities.ssrf.StoredSSRFException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;
import static dev.aikido.agent_api.storage.AttackQueue.attackDetected;

public final class DNSRecordCollector {
    private DNSRecordCollector() {}
    private static final Logger logger = LogManager.getLogger(DNSRecordCollector.class);
    private static final String INET_ADDRESS_OPERATION_NAME = "java.net.InetAddress.getAllByName";
    private static final String SOCKET_CHANNEL_OPERATION_NAME = "java.nio.channels.SocketChannel.connect";

    public static void report(String hostname, InetAddress[] inetAddresses) {
        // InetAddress.getAllByName() resolves everything in one call, so it's safe to consume.
        withCapturedContext(hostname, () ->
                process(hostname, inetAddresses, PendingHostnamesStore.getAndRemove(hostname), INET_ADDRESS_OPERATION_NAME));
    }

    // For clients that resolve their own DNS (e.g. Reactor Netty, used by Spring's WebClient) or
    // connect straight to an IP literal. A single request can trigger multiple connect() calls to
    // the same hostname (IPv4 then IPv6), so unlike report(), this peeks the pending port instead
    // of consuming it - consuming on the first attempt would let a later attempt bypass SSRF.
    public static void reportConnect(String hostname, InetAddress resolvedAddress) {
        withCapturedContext(hostname, () ->
                process(hostname, new InetAddress[]{resolvedAddress}, PendingHostnamesStore.getPorts(hostname), SOCKET_CHANNEL_OPERATION_NAME));
    }

    // Restores the ContextObject captured when this hostname's pending entry was registered
    // (PendingHostnamesStore is global, not thread-local) so SSRFDetector's Context.get() sees
    // the request that actually triggered the outbound call, even if we're running on a
    // different thread than the one that registered it.
    private static void withCapturedContext(String hostname, Runnable action) {
        ContextObject capturedContext = PendingHostnamesStore.getContext(hostname);
        if (capturedContext == null) {
            action.run();
            return;
        }
        ContextObject previous = Context.get();
        Context.set(capturedContext);
        try {
            action.run();
        } finally {
            if (previous != null) {
                Context.set(previous);
            } else {
                Context.reset();
            }
        }
    }

    private static void process(String hostname, InetAddress[] inetAddresses, Set<Integer> ports, String operationName) {
        try {
            logger.trace("DNSRecordCollector called with %s & inet addresses: %s", hostname, List.of(inetAddresses));

            // store stats
            StatisticsStore.registerCall(operationName, OperationKind.OUTGOING_HTTP_OP);

            // No pending port + private IP literal = infrastructure noise (Netty resolver bootstrap
            // etc), not a real request - skip recording/blocking. SSRF checks below still run regardless.
            if (!ports.isEmpty() || !IsPrivateIP.isPrivateIp(hostname)) {
                if (!ports.isEmpty()) {
                    for (int port : ports) {
                        HostnamesStore.incrementHits(hostname, port);
                    }
                } else {
                    // We still need to report a hit to the hostname for outbound domain blocking
                    HostnamesStore.incrementHits(hostname, 0);
                }

                // Block if the hostname is in the blocked domains list
                if (ServiceConfigStore.shouldBlockOutgoingRequest(hostname)) {
                    logger.debug("Blocking DNS lookup for domain: %s", hostname);
                    throw BlockedOutboundException.get();
                }
            }

            // Convert inetAddresses array to a List of IP strings :
            List<String> ipAddresses = new ArrayList<>();
            for (InetAddress inetAddress : inetAddresses) {
                ipAddresses.add(inetAddress.getHostAddress());
            }

            // Run SSRF check for all ports found in the pending store (empty = no SSRF check)
            for (int port : ports) {
                logger.debug("Hostname: %s, Port: %s, IPs: %s", hostname, port, ipAddresses);

                Attack attack = SSRFDetector.run(hostname, port, ipAddresses, operationName);
                if (attack == null) {
                    continue;
                }

                logger.debug("SSRF Attack detected due to: %s:%s", hostname, port);
                attackDetected(attack, Context.get());

                if (shouldBlock()) {
                    logger.debug("Blocking SSRF attack...");
                    throw SSRFException.get();
                }

                // We don't want to test for a stored SSRF attack.
                return;
            }

            // We don't need the context object to check for stored ssrf, but we do want to run this after our other
            // SSRF checks, making sure if it's a normal ssrf attack it gets reported like that.
            Attack storedSsrfAttack = new StoredSSRFDetector().run(hostname, ipAddresses, operationName);
            if (storedSsrfAttack != null) {
                attackDetected(storedSsrfAttack, Context.get());

                if (shouldBlock()) {
                    logger.debug("Blocking stored SSRF attack...");
                    throw StoredSSRFException.get();
                }
            }

        } catch (BlockedOutboundException | SSRFException | StoredSSRFException e) {
            throw e;
        } catch (Throwable e) {
            logger.trace(e);
        }
    }
}
