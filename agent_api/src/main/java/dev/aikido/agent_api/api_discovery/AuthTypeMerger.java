package dev.aikido.agent_api.api_discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AuthTypeMerger {
    private AuthTypeMerger() {}

    public static List<Map<String, String>> mergeAuthTypes(List<Map<String, String>> existing, List<Map<String, String>> newAuth) {
        // Check if newAuth is null or empty
        if (newAuth == null || newAuth.isEmpty()) {
            return existing;
        }

        // Check if existing is null or empty
        if (existing == null || existing.isEmpty()) {
            return newAuth;
        }

        List<Map<String, String>> result = new ArrayList<>(existing);

        for (Map<String, String> auth : newAuth) {
            if (!containsEqualAuthType(result, auth)) {
                result.add(auth);
            }
        }

        return result;
    }

    private static boolean containsEqualAuthType(List<Map<String, String>> list, Map<String, String> auth) {
        for (Map<String, String> a : list) {
            if (isEqualApiAuthType(a, auth)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEqualApiAuthType(Map<String, String> a, Map<String, String> b) {
        if (a == null || b == null) {
            return false; // Return false if either map is null
        }

        return Objects.equals(a.get("type"), b.get("type")) &&
                Objects.equals(a.get("in"), b.get("in")) &&
                Objects.equals(a.get("name"), b.get("name")) &&
                Objects.equals(a.get("scheme"), b.get("scheme"));
    }
}
