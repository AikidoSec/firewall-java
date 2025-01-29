package dev.aikido.agent_api.vulnerabilities.ssrf.imds;


import java.util.HashSet;
import java.util.Set;

public class BlockList {
    /**
     * A list of IPs that shouldn't be accessed
     */
    private final Set<String> blockedIPv4Addresses;
    private final Set<String> blockedIPv6Addresses;

    public BlockList() {
        this.blockedIPv4Addresses = new HashSet<>();
        this.blockedIPv6Addresses = new HashSet<>();
    }

    /**
     * Add an address to this list
     */
    public void addAddress(String address, String addressType) {
        if ("ipv4".equals(addressType)) {
            blockedIPv4Addresses.add(address);
        } else if ("ipv6".equals(addressType)) {
            blockedIPv6Addresses.add(address);
        }
    }

    /**
     * Check if the IP is on the list
     */
    public boolean check(String address, String addressType) {
        if (addressType != null) {
            if ("ipv4".equals(addressType)) {
                return blockedIPv4Addresses.contains(address);
            } else if ("ipv6".equals(addressType)) {
                return blockedIPv6Addresses.contains(address);
            }
        }
        return blockedIPv4Addresses.contains(address) || blockedIPv6Addresses.contains(address);
    }
}
