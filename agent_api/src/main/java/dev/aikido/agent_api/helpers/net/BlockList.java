package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import java.util.ArrayList;
import java.util.List;

public class BlockList {
    // private HashSet<String> blockedAddresses; // Both IPv4 and IPv6
    private List<IPAddress> blockedList;

    public BlockList() {
        // this.blockedAddresses = new HashSet<>();
        this.blockedList = new ArrayList<>();
    }

    public void add(String ipOrCIDR) {
        IPAddress ip = new IPAddressString(ipOrCIDR).getAddress();
        if (ipOrCIDR.contains("/")) {
            // CIDR :
            ip = ip.toPrefixBlock();
        }
        if (ip != null) {
            blockedList.add(ip);
        }
    }

    public boolean isBlocked(String ip) {
        IPAddressString ipAddressString = new IPAddressString(ip);
        if (!ipAddressString.isValid()) {
            return false; // Invalid IP address
        }
        IPAddress ipAddress = ipAddressString.getAddress();

        // Check if the IP address is in any of the blocked subnets
        for (IPAddress subnet : blockedList) {
            if (subnet.contains(ipAddress)) {
                return true;
            }
        }
        return false;
    }
}
