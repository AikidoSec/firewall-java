package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

import dev.aikido.agent_api.helpers.net.IPList;

public final class IMDSAddresses {
    private IMDSAddresses() {}
    private static final IPList imdsAddresses = new IPList();

    static {
        // Add the IP addresses used by AWS EC2 instances for IMDS
        imdsAddresses.add("169.254.169.254");
        imdsAddresses.add("fd00:ec2::254");

        // Add the IP addresses used for Alibaba Cloud
        imdsAddresses.add("100.100.100.200");
    }

    /** Checks if the IP is an IMDS IP */
    public static boolean isImdsIpAddress(String ip) {
        return imdsAddresses.matches(ip);
    }
}
