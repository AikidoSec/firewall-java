package dev.aikido.AikidoAgent.background.cloud;

import dev.aikido.AikidoAgent.Config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
        return new ManagerInfo(
            !connectionManager.shouldBlock(), // dryMode
            getHostname(), // hostname
            Config.pkgVersion, // version
            "firewall-java", // library
            getIpAddress(), // ipAddress
            Map.of(), // packages (FIX LATER)
            connectionManager.getServerless(), // serverless
            List.of(), // stack
            getOSInfo(), // os
            false, // preventedPrototypePollution, should be removed from API
            "", // nodeEnv
            getPlatformInfo() // platform info
        );
    }

    private static String getHostname() {
        // getHostName function seem unreliable, so using "hostname" command which works for both UNIX(-like) systems and Windows
        // See https://stackoverflow.com/a/7800008 for more info.
        try (Scanner s = new Scanner(Runtime.getRuntime().exec("hostname").getInputStream()).useDelimiter("\\A")) {
            if (s.hasNext()) {
                return s.next().trim();
            }
        } catch (IOException ignored) {
        }
        return "unknown";
    }

    private static String getIpAddress() {
        // Fix later :
        return "0.0.0.0";
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
