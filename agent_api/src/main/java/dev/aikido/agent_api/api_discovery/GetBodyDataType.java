package dev.aikido.agent_api.api_discovery;

import java.util.List;
import java.util.Map;

public final class GetBodyDataType {
    private GetBodyDataType() {}

    private static final List<String> JSON_CONTENT_TYPES = List.of(
            "application/json",
            "application/vnd.api+json",
            "application/csp-report",
            "application/x-json"
    );

    public static String getBodyDataType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return null;
        }

        // Check for JSON content types
        for (String jsonType : JSON_CONTENT_TYPES) {
            if (contentType.contains(jsonType)) {
                return "json";
            }
        }

        if (contentType.startsWith("application/x-www-form-urlencoded")) {
            return "form-urlencoded";
        } else if (contentType.startsWith("multipart/form-data")) {
            return "form-data";
        } else if (contentType.contains("xml")) {
            return "xml";
        }

        return null;
    }
}
