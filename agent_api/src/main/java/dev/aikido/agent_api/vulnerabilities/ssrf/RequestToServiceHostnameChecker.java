package dev.aikido.agent_api.vulnerabilities.ssrf;

import java.util.List;
import java.util.regex.Pattern;

public final class RequestToServiceHostnameChecker {
    // Pattern allows alpha input (case-insensitive), dashes (-) and underscores (_)
    private static final Pattern SERVICE_HOSTNAME_PATTERN = Pattern.compile("^[a-zA-Z-_]+$");
    private static final List ALLOWED_LOCALHOST_VARIANTS = List.of(
        "localhost",
        "localdomain",
        // metadata is a hostname used by google, it gets automatically extended to metadata.goog
        // https://stackoverflow.com/questions/23362887/can-you-get-external-ip-address-from-within-a-google-compute-vm-instance
        "metadata"
    );

    public static boolean isRequestToServiceHostname(String hostname) {
        if (hostname == null) {
            return false;
        }
        if (ALLOWED_LOCALHOST_VARIANTS.contains(hostname.toLowerCase())) {
            // "localhost" or its variants are not service hostnames
            return false;
        }

        return SERVICE_HOSTNAME_PATTERN.matcher(hostname).matches();
    }
}
