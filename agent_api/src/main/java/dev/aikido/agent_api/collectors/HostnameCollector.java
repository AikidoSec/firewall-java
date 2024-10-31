package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class HostnameCollector {
    private static final Logger logger = LogManager.getLogger(HostnameCollector.class);
    public static void report(String hostname, InetAddress[] inetAddresses) {
        // Convert inetAddresses array to a List of IP strings :
        List<String> ipAddresses = new ArrayList<>();
        for(InetAddress inetAddress: inetAddresses) {
            ipAddresses.add(inetAddress.getHostAddress());
        }
        // Currently using hostnames from thread cache, might not be as accurate as using Context-dependant hostnames.
        for (Hostnames.HostnameEntry hostnameEntry: ThreadCache.get().getHostnames().asArray()) {
            if(!hostnameEntry.getHostname().equals(hostname)) {
                continue;
            }
            logger.debug("Hostname: {}, Port: {}, IPs: {}", hostnameEntry.getHostname(), hostnameEntry.getPort(), ipAddresses);
        }
    }
}
