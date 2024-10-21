package dev.aikido.AikidoAgent.background.cloud;

import dev.aikido.AikidoAgent.Config;
import dev.aikido.AikidoAgent.background.ServiceConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static dev.aikido.AikidoAgent.helpers.net.Hostname.getHostname;
import static dev.aikido.AikidoAgent.helpers.net.IPAddress.getIpAddress;

/**
 * Class to give you the "agent" info, which is the CloudConnectionManager in Java.
 */
public class GetManagerInfo {
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
        ServiceConfiguration serviceConfig = connectionManager.getConfig();
        return new ManagerInfo(
            !connectionManager.shouldBlock(), // dryMode
            getHostname(), // hostname
            Config.pkgVersion, // version
            "firewall-java", // library
            getIpAddress(), // ipAddress
            Map.of(), // packages (FIX LATER)
            serviceConfig.getServerless(), // serverless
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
