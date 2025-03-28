package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.helpers.net.IPList;
import inet.ipaddr.IPAddressString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IsPrivateIP {
    // Define private IP ranges
    private static final List<String> PRIVATE_IP_RANGES = List.of(
            "0.0.0.0/8", // "This" network (RFC 1122)
            "10.0.0.0/8", // Private-Use Networks (RFC 1918)
            "100.64.0.0/10", // Shared Address Space (RFC 6598)
            "127.0.0.0/8", // Loopback (RFC 1122)
            "169.254.0.0/16", // Link Local (RFC 3927)
            "172.16.0.0/12", // Private-Use Networks (RFC 1918)
            "192.0.0.0/24", // IETF Protocol Assignments (RFC 5736)
            "192.0.2.0/24", // TEST-NET-1 (RFC 5737)
            "192.31.196.0/24", // AS112 Redirection Anycast (RFC 7535)
            "192.52.193.0/24", // Automatic Multicast Tunneling (RFC 7450)
            "192.88.99.0/24", // 6to4 Relay Anycast (RFC 3068)
            "192.168.0.0/16", // Private-Use Networks (RFC 1918)
            "192.175.48.0/24", // AS112 Redirection Anycast (RFC 7535)
            "198.18.0.0/15", // Network Interconnect Device Benchmark Testing (RFC 2544)
            "198.51.100.0/24", // TEST-NET-2 (RFC 5737)
            "203.0.113.0/24", // TEST-NET-3 (RFC 5737)
            "240.0.0.0/4", // Reserved for Future Use (RFC 1112)
            "224.0.0.0/4", // Multicast (RFC 3171)
            "255.255.255.255/32" // Limited Broadcast (RFC 919)
    );
    private static final List<String> PRIVATE_IPV6_RANGES = List.of(
            "::/128", // Unspecified address (RFC 4291)
            "::1/128", // Loopback address (RFC 4291)
            "fc00::/7", // Unique local address (ULA) (RFC 4193)
            "fe80::/10", // Link-local address (LLA) (RFC 4291)
            "100::/64", // Discard prefix (RFC 6666)
            "2001:db8::/32", // Documentation prefix (RFC 3849)
            "3fff::/20" // Documentation prefix (RFC 9637)
    );
    private static final IPList privateIpNetworks = new IPList();

    static {
        PRIVATE_IP_RANGES.stream().forEach(privateIpNetworks::add);
        PRIVATE_IPV6_RANGES.stream().forEach(privateIpNetworks::add);
        // Add IPv4-mapped IPv6 addresses
        for (String ipv4Ranges: PRIVATE_IP_RANGES) {
            privateIpNetworks.add(mapIPv4ToIPv6(ipv4Ranges));
        }
    }

    private IsPrivateIP() {
    }

    public static boolean containsPrivateIP(List<String> ipAddresses) {
        for (String ip : ipAddresses) {
            if (isPrivateIp(ip)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPrivateIp(String ip) {
        return privateIpNetworks.matches(ip);
    }

    /**
     * Maps an IPv4 address to an IPv6 address.
     * e.g. 127.0.0.0/8 -> ::ffff:127.0.0.0/104
     */
    public static String mapIPv4ToIPv6(String ip) {
        if (!ip.contains("/")) {
            // No CIDR suffix, assume /32
            return "::ffff:" + ip + "/128";
        }

        String[] parts = ip.split("/");
        int suffix = Integer.parseInt(parts[1]);
        // We add 96 to the suffix, since ::ffff: already is 96 bits,
        // so the 32 remaining bits are decided by the IPv4 address
        return "::ffff:" + parts[0] + "/" + (suffix + 96);
    }
}
