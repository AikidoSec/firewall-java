package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

import dev.aikido.agent_api.helpers.net.IPList;
import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.mapIPv4ToIPv6;

public final class IMDSAddresses {
    private IMDSAddresses() {}
    private static final IPList imdsAddresses = new IPList();

    static {
        // Add the IP addresses used by AWS EC2 instances for IMDS
        imdsAddresses.add("169.254.169.254");
        imdsAddresses.add("fd00:ec2::254");
        imdsAddresses.add(mapIPv4ToIPv6("169.254.169.254"));

        // Add the IP addresses used for Alibaba Cloud
        imdsAddresses.add("100.100.100.200");
        imdsAddresses.add(mapIPv4ToIPv6("169.254.169.254"));
    }

    /** Checks if the IP is an IMDS IP */
    public static boolean isImdsIpAddress(String ip) {
        return imdsAddresses.matches(ip);
    }
}
