package dev.aikido.agent_api.helpers.patterns;

public final class HttpAuthScheme {
    private HttpAuthScheme() {
    }

    private static final String[] AUTH_SCHEMES = {
        "basic", "bearer", "digest",
        "dpop", "gnap", "hoba",
        "mutual", "negotiate", "privatetoken",
        "scram-sha-1", "scram-sha-256", "vapid"
    };

    public static boolean isHttpAuthScheme(String scheme) {
        for (String authScheme : AUTH_SCHEMES) {
            if (authScheme.equalsIgnoreCase(scheme)) {
                return true;
            }
        }
        return false;
    }
}
