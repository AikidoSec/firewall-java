package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressNetwork;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class BlockList {
    private HashSet<String> blockedAddresses; // Both IPv4 and IPv6
    private List<IPAddress> blockedSubnets;

    public BlockList() {
        this.blockedAddresses = new HashSet<>();
        this.blockedSubnets = new ArrayList<>();
    }

    public void addAddress(String ip) {
        blockedAddresses.add(ip);
    }

    public void addSubnet(String plainIp, int ipRange) {
        IPAddress subnet = new IPAddressString(plainIp + "/" + ipRange).getAddress();
        if (subnet != null) {
            blockedSubnets.add(subnet);
        }
    }

    public boolean isBlocked(String ip) {
        // Check if the IP address is in the blocked addresses
        if (blockedAddresses.contains(ip)) {
            return true;
        }

        IPAddressString ipAddressString = new IPAddressString(ip);
        if (!ipAddressString.isValid()) {
            return false; // Invalid IP address
        }
        IPAddress ipAddress = ipAddressString.getAddress();

        // Check if the IP address is in any of the blocked subnets
        for (IPAddress subnet : blockedSubnets) {
            if (subnet.contains(ipAddress)) {
                return true;
            }
        }

        return false;
    }
}
