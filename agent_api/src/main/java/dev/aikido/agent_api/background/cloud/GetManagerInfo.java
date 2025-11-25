package dev.aikido.agent_api.background.cloud;

import dev.aikido.agent_api.Config;
import dev.aikido.agent_api.helpers.net.Hostname;
import dev.aikido.agent_api.helpers.net.IPAddress;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.ServiceConfiguration;

import java.util.List;
import java.util.Map;

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
    private static final OS OS_INFO;
    static {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        OS_INFO = new OS(osName, osVersion);
    }

    public record Platform(String name, String version) {}
    private static final Platform PLATFORM_INFO;
    static {
        String jvmName = System.getProperty("java.vm.name");
        String jvmVersion = System.getProperty("java.version");
        PLATFORM_INFO = new Platform(jvmName, jvmVersion);
    }


    public static ManagerInfo getManagerInfo() {
        ServiceConfiguration serviceConfig = ServiceConfigStore.getConfig();
        return new ManagerInfo(
            !serviceConfig.isBlockingEnabled(), // dryMode
            Hostname.get(), // hostname
            Config.pkgVersion, // version
            "firewall-java", // library
            IPAddress.get(), // ipAddress
            Map.of(), // packages (FIX LATER)
            null, // serverless is not supported for Java
            List.of(), // stack
            OS_INFO, // os
            false, // preventedPrototypePollution, should be removed from API
            "", // nodeEnv
            PLATFORM_INFO // platform info
        );
    }
}
