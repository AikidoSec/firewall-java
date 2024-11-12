package dev.aikido.agent_api.collectors;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.ipc_commands.AttackCommand;
import dev.aikido.agent_api.background.utilities.IPCClient;
import dev.aikido.agent_api.background.utilities.IPCDefaultClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFDetector;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;

public final class HostnameCollector {
    private HostnameCollector() {}
    private static final Logger logger = LogManager.getLogger(HostnameCollector.class);
    public static void report(String hostname, InetAddress[] inetAddresses) {
        // Convert inetAddresses array to a List of IP strings :
        List<String> ipAddresses = new ArrayList<>();
        for(InetAddress inetAddress: inetAddresses) {
            ipAddresses.add(inetAddress.getHostAddress());
        }
        // Currently using hostnames from thread cache, might not be as accurate as using Context-dependant hostnames.
        if (ThreadCache.get() == null || ThreadCache.get().getHostnames() == null) {
            return;
        }
        for (Hostnames.HostnameEntry hostnameEntry: ThreadCache.get().getHostnames().asArray()) {
            if(!hostnameEntry.getHostname().equals(hostname)) {
                continue;
            }
            logger.debug("Hostname: {}, Port: {}, IPs: {}", hostnameEntry.getHostname(), hostnameEntry.getPort(), ipAddresses);

            Attack attack = new SSRFDetector().run(
                    hostname, hostnameEntry.getPort(), ipAddresses,
                    /* operation: */ "java.net.InetAddress.getAllByName");
            if(attack == null) {
                continue;
            }

            Gson gson = new Gson();
            String json = gson.toJson(new AttackCommand.AttackCommandData(attack, Context.get()));

            IPCClient client = new IPCDefaultClient();
            client.sendData(
                    "ATTACK$" + json, // data
                    false // receive
            );
            logger.info("Sent attack data");
            if(shouldBlock()) {
                logger.debug("Blocking SSRF attack...");
                throw SSRFException.get();
            }
        }
    }
}
