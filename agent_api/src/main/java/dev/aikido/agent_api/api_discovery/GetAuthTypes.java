package dev.aikido.agent_api.api_discovery;

import dev.aikido.agent_api.context.ContextObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.helpers.patterns.HttpAuthScheme.isHttpAuthScheme;

public class GetAuthTypes {
    private static final String[] COMMON_API_KEY_HEADER_NAMES = {
            "x-api-key", "api-key", "apikey", "x-token", "token"
    };
    private static final String[] COMMON_AUTH_COOKIE_NAMES = {
            "auth", "session", "jwt",
            "token", "sid", "connect.sid",
            "auth_token", "access_token", "refresh_token"
    };

    public static List<Map<String, String>> getAuthTypes(ContextObject context) {
        if (context.getHeaders() == null) {
            return null;
        }
        List<Map<String, String>> result = new ArrayList<>();

        // Check the Authorization header
        String authHeader = context.getHeaders().get("authorization");
        Map<String, String> authHeaderType = getAuthorizationHeaderType(authHeader);
        if (authHeaderType != null) {
            result.add(authHeaderType);
        }
        // Check for type apiKey in headers and cookies
        result.addAll(findApiKeys(context));

        return result.isEmpty() ? null : result;
    }

    private static Map<String, String> getAuthorizationHeaderType(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String[] parts = authHeader.split(" ", 2);
        if (parts.length == 2) {
            String scheme = parts[0];
            if (isHttpAuthScheme(scheme)) {
                return Map.of("type", "http", "scheme", scheme.toLowerCase());
            }
        }

        // Default to apiKey if the auth type is not recognized
        return Map.of("type", "apiKey", "in", "header", "name", "Authorization");
    }
    private static List<Map<String, String>> findApiKeys(ContextObject context) {
        List<Map<String, String>> result = new ArrayList<>();

        for (String header : COMMON_API_KEY_HEADER_NAMES) {
            if (context.getHeaders().containsKey(header)) {
                Map<String, String> apiKeyInfo = Map.of("type", "apiKey", "in", "header", "name", header);
                result.add(apiKeyInfo);
            }
        }

        if (context.getCookies() != null) {
            for (String cookie : context.getCookies().keySet()) {
                String lowerCaseCookie = cookie.toLowerCase();
                for (String commonCookie : COMMON_AUTH_COOKIE_NAMES) {
                    if (lowerCaseCookie.equals(commonCookie)) {
                        Map<String, String> apiKeyInfo = Map.of("type", "apiKey", "in", "cookie", "name", cookie);
                        result.add(apiKeyInfo);
                    }
                }
            }
        }

        return result;
    }
}
