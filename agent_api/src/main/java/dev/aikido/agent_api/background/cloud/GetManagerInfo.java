package dev.aikido.agent_api.background.cloud;

import dev.aikido.agent_api.Config;
import dev.aikido.agent_api.storage.Configuration;

import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.helpers.net.Hostname.getHostname;
import static dev.aikido.agent_api.helpers.net.IPAddress.getIpAddress;
import static dev.aikido.agent_api.storage.ConfigStore.getConfig;

/**
 * Class to give you the "agent" info, which is the CloudConnectionManager in Java.
 */
public final class GetManagerInfo {
    private GetManagerInfo() {}
    public record ManagerInfo(
            boolean dryMode,
            String hostname,
            String version,
            String library,
            String ipAddress,
            Map<String, String> packages,
            String serverless,
            List<String> stack,
            OS os,
            boolean preventedPrototypePollution,
            String nodeEnv,
            Platform platform
    ) {}
    public record OS(String name, String version) {}

    public record Platform(String name, String version) {}

    public static ManagerInfo getManagerInfo(CloudConnectionManager connectionManager) {
        Configuration config = getConfig();
        return new ManagerInfo(
            !config.isBlockingEnabled(), // dryMode
            getHostname(), // hostname
            Config.pkgVersion, // version
            "firewall-java", // library
            getIpAddress(), // ipAddress
            Map.of(), // packages (FIX LATER)
            null, // serverless is not supported for Java
            List.of(), // stack
            getOSInfo(), // os
            false, // preventedPrototypePollution, should be removed from API
            "", // nodeEnv
            getPlatformInfo() // platform info
        );
    }

    private static OS getOSInfo() {
        String name = System.getProperty("os.name");
        String version = System.getProperty("os.version");
        return new OS(name, version);
    }

    private static Platform getPlatformInfo() {
        String name = System.getProperty("java.vm.name");
        String version = System.getProperty("java.version");
        return new Platform(name, version);
    }
}
