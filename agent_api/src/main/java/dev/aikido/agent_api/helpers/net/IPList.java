package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import java.util.List;
import java.util.ArrayList;

public class IPList {
    private List<IPAddress> ipAddresses;

    public IPList() {
        this.ipAddresses = new ArrayList<>();
    }

    public void add(String ipOrCIDR) {
        IPAddress ip = new IPAddressString(ipOrCIDR).getAddress();
        if (ipOrCIDR.contains("/")) {
            // CIDR :
            ip = ip.toPrefixBlock();
        }
        if (ip != null) {
            ipAddresses.add(ip);
        }
    }

    public boolean matches(String ip) {
        IPAddressString ipAddressString = new IPAddressString(ip);
        if (!ipAddressString.isValid()) {
            return false; // Invalid IP address
        }
        IPAddress ipAddress = ipAddressString.getAddress();

        // Check if the IP address is in any of the blocked subnets
        for (IPAddress subnet : ipAddresses) {
            if (subnet.contains(ipAddress)) {
                return true;
            }
        }
        return false;
    }
}
