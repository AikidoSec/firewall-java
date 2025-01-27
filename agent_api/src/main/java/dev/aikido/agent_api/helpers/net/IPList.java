package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.util.List;
import java.util.ArrayList;

/** IPList
 * is used for blocklists, allowlists, ... e.g. geo ip restrictions.
 */
public class IPList {
    //private HashSet<String> blockedAddresses; // Both IPv4 and IPv6
    private List<IPAddress> ipList;

    public IPList() {
        //this.blockedAddresses = new HashSet<>();
        this.ipList = new ArrayList<>();
    }

    public void add(String ipOrCIDR) {
        IPAddress ip = new IPAddressString(ipOrCIDR).getAddress();
        if (ipOrCIDR.contains("/")) {
            // CIDR :
            ip = ip.toPrefixBlock();
        }
        if (ip != null) {
            ipList.add(ip);
        }
    }

    public boolean contains(String ip) {
        IPAddressString ipAddressString = new IPAddressString(ip);
        if (!ipAddressString.isValid()) {
            return false; // Invalid IP address
        }
        IPAddress ipAddress = ipAddressString.getAddress();

        // Check if the IP address is in any of the blocked subnets
        for (IPAddress subnet : ipList) {
            if (subnet.contains(ipAddress)) {
                return true;
            }
        }
        return false;
    }
}
