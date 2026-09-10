package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

import dev.aikido.agent_api.helpers.net.IPList;
import java.util.List;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPListWithMappedAddresses;

public final class IMDSAddresses {
    private IMDSAddresses() {}

    // Small list, frequently accessed: add IPv4-mapped versions at creation time for fast lookups
    private static final IPList imdsAddresses = createIPListWithMappedAddresses(List.of(
            "169.254.169.254", // AWS EC2
            "fd00:ec2::254", // AWS EC2
            "100.100.100.200" // Alibaba Cloud
    ));

    /** Checks if the IP is an IMDS IP */
    public static boolean isImdsIpAddress(String ip) {
        return imdsAddresses.matches(ip);
    }
}
