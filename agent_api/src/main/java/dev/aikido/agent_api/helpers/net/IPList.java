package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.util.HashSet;
import java.util.Set;

public class IPList {
    private Set<IPAddress> ipAddresses;

    public IPList() {
        this.ipAddresses = new HashSet<>();
    }

    public void add(String ipOrCIDR) {
        if (ipOrCIDR == null) {
            return; // Don't add if IP is null
        }
        IPAddress ip = new IPAddressString(ipOrCIDR).getAddress();
        if (ip == null) {
            return;
        }
        // Normalize IPv4-mapped IPv6 addresses to their IPv4 form so matching is symmetric.
        if (ip.isIPv6() && ip.toIPv6().isIPv4Convertible()) {
            IPAddress ipv4 = ip.toIPv6().toIPv4();
            if (ipv4 != null) {
                ip = ipv4;
            }
        }
        if (ipOrCIDR.contains("/")) {
            ip = ip.toPrefixBlock();
        }
        ipAddresses.add(ip);
    }

    public boolean matches(String ip) {
        IPAddressString ipAddressString = new IPAddressString(ip);
        if (!ipAddressString.isValid()) {
            return false; // Invalid IP address
        }
        IPAddress ipAddress = ipAddressString.getAddress();

        if (containsAddress(ipAddress)) {
            return true;
        }

        // Also try the embedded IPv4 form for IPv4-mapped IPv6 addresses (e.g. ::ffff:23.45.67.89)
        if (ipAddress.isIPv6() && ipAddress.toIPv6().isIPv4Convertible()) {
            IPAddress ipv4 = ipAddress.toIPv6().toIPv4();
            if (ipv4 != null && containsAddress(ipv4)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAddress(IPAddress ipAddress) {
        for (IPAddress subnet : ipAddresses) {
            if (subnet.contains(ipAddress)) {
                return true;
            }
        }
        return false;
    }
    public int length() {
        return ipAddresses.size();
    }
}
