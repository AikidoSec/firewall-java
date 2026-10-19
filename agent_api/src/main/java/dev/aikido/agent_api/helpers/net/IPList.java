package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;

import java.util.Collection;
import java.util.List;

// The networks come from Aikido, not from users, so the matcher doesn't validate them strictly:
// "1.2.3.4abc" is stored as 1.2.3.4.
public class IPList {
    private volatile IPMatcher matcher;

    public IPList() {
        this(List.of());
    }

    public IPList(Collection<String> ipsOrCIDRs) {
        this.matcher = new IPMatcher(normalizeNetworks(ipsOrCIDRs));
    }

    public void add(String ipOrCIDR) {
        if (ipOrCIDR == null) {
            return; // Don't add if IP is null
        }
        matcher = matcher.withNetworks(List.of(ipv4IfMapped(ipOrCIDR)));
    }

    public boolean matches(String ip) {
        return matcher.has(ip);
    }

    public int length() {
        return matcher.size();
    }

    private static List<String> normalizeNetworks(Collection<String> ipsOrCIDRs) {
        if (ipsOrCIDRs == null) {
            return List.of();
        }
        return ipsOrCIDRs.stream().map(IPList::ipv4IfMapped).toList();
    }

    // The matcher matches IPv4-mapped lookups against IPv4 networks, not IPv4 lookups against mapped networks.
    // So we store "::ffff:1.2.3.4" as "1.2.3.4".
    private static String ipv4IfMapped(String network) {
        if (!mayBeIpv4Mapped(network)) {
            return network;
        }
        try {
            IPAddress address = new IPAddressString(network).getAddress();
            if (address == null || !address.isIPv6()) {
                return network;
            }
            IPv4Address ipv4 = address.toIPv6().toIPv4();
            return ipv4 == null ? network : ipv4.toNormalizedString();
        } catch (RuntimeException e) {
            return network;
        }
    }

    // Avoids running the IP parser on every entry, lists can be big.
    private static boolean mayBeIpv4Mapped(String network) {
        return network != null && network.contains(":") && network.toLowerCase().contains("ffff");
    }
}
