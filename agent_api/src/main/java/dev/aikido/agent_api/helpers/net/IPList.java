package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.util.Collection;
import java.util.List;

public class IPList {
    private volatile IPMatcher matcher;

    public IPList() {
        this.matcher = new IPMatcher(List.of());
    }

    public IPList(Collection<String> ipsOrCIDRs) {
        this.matcher = new IPMatcher(ipsOrCIDRs == null ? null : ipsOrCIDRs.stream().map(IPList::ipv4IfMapped).toList());
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

    // The matcher matches IPv4-mapped lookups against IPv4 networks, not IPv4 lookups against mapped networks.
    private static String ipv4IfMapped(String network) {
        if (network == null || network.indexOf(':') < 0 || !network.toLowerCase().contains("ffff")) {
            return network;
        }
        try {
            IPAddress ip = new IPAddressString(network).getAddress();
            IPAddress ipv4 = ip != null && ip.isIPv6() ? ip.toIPv6().toIPv4() : null;
            return ipv4 != null ? ipv4.toNormalizedString() : network;
        } catch (RuntimeException e) {
            return network;
        }
    }
}
