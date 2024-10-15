package dev.aikido.AikidoAgent.helpers.url;

import java.util.Arrays;
import java.util.List;

public class IsUsefulRoute {

    private static final List<String> EXCLUDED_METHODS = Arrays.asList("OPTIONS", "HEAD");
    private static final List<String> IGNORE_EXTENSIONS = Arrays.asList("properties", "php", "asp", "aspx", "jsp", "config");
    private static final List<String> IGNORE_STRINGS = List.of("cgi-bin");

    public static boolean isUsefulRoute(int statusCode, String route, String method) {
        // Check if the status code is valid
        if (!isValidStatusCode(statusCode)) {
            return false;
        }

        // Check if the method is excluded
        if (EXCLUDED_METHODS.contains(method)) {
            return false;
        }

        // Split the route into segments
        String[] segments = route.split("/");

        // Check for dot files
        for (String segment : segments) {
            if (isDotFile(segment) || containsIgnoredString(segment)) {
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
        if (segment.equals(".well-known")) {
            return false;
        }
        return segment.startsWith(".") && segment.length() > 1;
    }

    private static boolean containsIgnoredString(String segment) {
        return IGNORE_STRINGS.stream().anyMatch(segment::contains);
    }
}
