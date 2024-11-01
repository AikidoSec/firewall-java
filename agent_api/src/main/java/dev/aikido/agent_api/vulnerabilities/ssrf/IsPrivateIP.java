package dev.aikido.agent_api.vulnerabilities.ssrf;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IsPrivateIP {
    public static boolean containsPrivateIP(List<String> ipAddresses) {
        for (String ip : ipAddresses) {
            if (isPrivateIp(ip)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPrivateIp(String ip) {
        for (String network : privateIpNetworks) {
            if (isInRange(ip, network)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInRange(String ipAddress, String range) {
        try {
            return new IPAddressString(range).toAddress()
                    .toSequentialRange()
                    .contains(new IPAddressString(ipAddress).toAddress());
        } catch (Exception e) {
            return false; // Handle any exceptions that may occur
        }
    }



    // Define private IP ranges
    private static final String[] PRIVATE_IP_RANGES = {
            "0.0.0.0/8",
            "10.0.0.0/8",
            "100.64.0.0/10",
            "127.0.0.0/8",
            "169.254.0.0/16",
            "172.16.0.0/12",
            "192.0.0.0/24",
            "192.0.2.0/24",
            "192.31.196.0/24",
            "192.52.193.0/24",
            "192.88.99.0/24",
            "192.168.0.0/16",
            "192.175.48.0/24",
            "198.18.0.0/15",
            "198.51.100.0/24",
            "203.0.113.0/24",
            "240.0.0.0/4",
            "224.0.0.0/4",
            "255.255.255.255/32"
    };

    private static final String[] PRIVATE_IPV6_RANGES = {
            "::/128",  // Unspecified address
            "::1/128",  // Loopback address
            "fc00::/7",  // Unique local address (ULA)
            "fe80::/10",  // Link-local address (LLA)
            "::ffff:127.0.0.1/128"  // IPv4-mapped address
    };

    private static final Set<String> privateIpNetworks = new HashSet<>();
    static {
        privateIpNetworks.addAll(Arrays.asList(PRIVATE_IP_RANGES));
        privateIpNetworks.addAll(Arrays.asList(PRIVATE_IPV6_RANGES));
    }
}
