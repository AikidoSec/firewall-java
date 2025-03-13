package dev.aikido.agent_api.helpers.url;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class IsUsefulRoute {
    private IsUsefulRoute() {}

    private static final List<String> EXCLUDED_METHODS = Arrays.asList("OPTIONS", "HEAD");
    private static final List<String> IGNORE_EXTENSIONS = Arrays.asList("properties", "config", "webmanifest");
    private static final List<String> IGNORE_STRINGS = List.of("cgi-bin");

    private static final Set<String> WELL_KNOWN_URIS = Set.of(
        "/.well-known/acme-challenge",
        "/.well-known/amphtml",
        "/.well-known/api-catalog",
        "/.well-known/appspecific",
        "/.well-known/ashrae",
        "/.well-known/assetlinks.json",
        "/.well-known/broadband-labels",
        "/.well-known/brski",
        "/.well-known/caldav",
        "/.well-known/carddav",
        "/.well-known/change-password",
        "/.well-known/cmp",
        "/.well-known/coap",
        "/.well-known/coap-eap",
        "/.well-known/core",
        "/.well-known/csaf",
        "/.well-known/csaf-aggregator",
        "/.well-known/csvm",
        "/.well-known/did.json",
        "/.well-known/did-configuration.json",
        "/.well-known/dnt",
        "/.well-known/dnt-policy.txt",
        "/.well-known/dots",
        "/.well-known/ecips",
        "/.well-known/edhoc",
        "/.well-known/enterprise-network-security",
        "/.well-known/enterprise-transport-security",
        "/.well-known/est",
        "/.well-known/genid",
        "/.well-known/gnap-as-rs",
        "/.well-known/gpc.json",
        "/.well-known/gs1resolver",
        "/.well-known/hoba",
        "/.well-known/host-meta",
        "/.well-known/host-meta.json",
        "/.well-known/hosting-provider",
        "/.well-known/http-opportunistic",
        "/.well-known/idp-proxy",
        "/.well-known/jmap",
        "/.well-known/keybase.txt",
        "/.well-known/knx",
        "/.well-known/looking-glass",
        "/.well-known/masque",
        "/.well-known/matrix",
        "/.well-known/mercure",
        "/.well-known/mta-sts.txt",
        "/.well-known/mud",
        "/.well-known/nfv-oauth-server-configuration",
        "/.well-known/ni",
        "/.well-known/nodeinfo",
        "/.well-known/nostr.json",
        "/.well-known/oauth-authorization-server",
        "/.well-known/oauth-protected-resource",
        "/.well-known/ohttp-gateway",
        "/.well-known/openid-federation",
        "/.well-known/open-resource-discovery",
        "/.well-known/openid-configuration",
        "/.well-known/openorg",
        "/.well-known/oslc",
        "/.well-known/pki-validation",
        "/.well-known/posh",
        "/.well-known/privacy-sandbox-attestations.json",
        "/.well-known/private-token-issuer-directory",
        "/.well-known/probing.txt",
        "/.well-known/pvd",
        "/.well-known/rd",
        "/.well-known/related-website-set.json",
        "/.well-known/reload-config",
        "/.well-known/repute-template",
        "/.well-known/resourcesync",
        "/.well-known/sbom",
        "/.well-known/security.txt",
        "/.well-known/ssf-configuration",
        "/.well-known/sshfp",
        "/.well-known/stun-key",
        "/.well-known/terraform.json",
        "/.well-known/thread",
        "/.well-known/time",
        "/.well-known/timezone",
        "/.well-known/tdmrep.json",
        "/.well-known/tor-relay",
        "/.well-known/tpcd",
        "/.well-known/traffic-advice",
        "/.well-known/trust.txt",
        "/.well-known/uma2-configuration",
        "/.well-known/void",
        "/.well-known/webfinger",
        "/.well-known/webweaver.json",
        "/.well-known/wot"
    );

    public static boolean isUsefulRoute(int statusCode, String route, String method) {
        // Check if the status code is valid
        if (!isValidStatusCode(statusCode)) {
            return false;
        }

        // Check if the method is excluded
        if (EXCLUDED_METHODS.contains(method)) {
            return false;
        }

        // Check if the route is a well-known URI
        boolean isWellKnownURI = isWellKnownURI(route);

        // Split the route into segments
        String[] segments = route.split("/");

        // Check for dot files
        for (String segment : segments) {
            // Do not discover routes with dot files like `/path/to/.file` or `/.directory/file`
            // We want to allow discovery of well-known URIs like `/.well-known/acme-challenge`
            if (!isWellKnownURI && isDotFile(segment)) {
                return false;
            }

            if (containsIgnoredString(segment)) {
                return false;
            }

            // Check for allowed extensions
            if (!isAllowedExtension(segment)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 400;
    }

    private static boolean isAllowedExtension(String segment) {
        // Get the file extension
        String extension = getExtension(segment);

        // Check if the extension is valid
        if (extension != null && !extension.isEmpty()) {
            // Check the length of the extension
            if (extension.length() >= 2 && extension.length() <= 5) {
                return false; // Invalid length
            }

            // Check if the extension is in the ignored list
            if (IGNORE_EXTENSIONS.contains(extension)) {
                return false; // Ignored extension
            }
        }

        return true; // Allowed extension
    }

    private static String getExtension(String segment) {
        int lastDotIndex = segment.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < segment.length() - 1) {
            return segment.substring(lastDotIndex + 1);
        }
        return null; // No extension found
    }

    private static boolean isDotFile(String segment) {
        return segment.startsWith(".") && segment.length() > 1;
    }

    private static boolean containsIgnoredString(String segment) {
        return IGNORE_STRINGS.stream().anyMatch(segment::contains);
    }

    // Check if a path is a well-known URI
    // e.g. /.well-known/acme-challenge
    // https://www.iana.org/assignments/well-known-uris/well-known-uris.xhtml
    private static boolean isWellKnownURI(String path) {
        return WELL_KNOWN_URIS.contains(path);
    }
}
