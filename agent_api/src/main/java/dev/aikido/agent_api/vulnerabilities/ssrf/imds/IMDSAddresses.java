package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

public class IMDSAddresses {
    private static final BlockList imdsAddresses = new BlockList();

    static {
        // Add the IP addresses used by AWS EC2 instances for IMDS
        imdsAddresses.addAddress("169.254.169.254", "ipv4");
        imdsAddresses.addAddress("fd00:ec2::254", "ipv6");
    }

    /** Checks if the IP is an IMDS IP */
    public static boolean isImdsIpAddress(String ip) {
        return imdsAddresses.check(ip, "ipv4") || imdsAddresses.check(ip, "ipv6");
    }
}