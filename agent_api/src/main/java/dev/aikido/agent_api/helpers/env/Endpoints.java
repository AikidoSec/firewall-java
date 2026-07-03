package dev.aikido.agent_api.helpers.env;

public final class Endpoints {
    private Endpoints() {}
    public static String getAikidoAPIEndpoint(Token token) {
        String endpoint = System.getenv("AIKIDO_ENDPOINT");
        if (endpoint != null && !endpoint.isEmpty()) {
            if (!endpoint.endsWith("/")) {
                return endpoint + "/"; // Make sure there is a trailing slash
            }
            return endpoint;
        }

        String region = token != null ? token.getRegion() : "EU";
        return switch (region) {
            case "US" -> "https://guard.us.aikido.dev/";
            case "ME" -> "https://guard.me.aikido.dev/";
            case "AU" -> "https://guard.au.aikido.dev/";
            default -> "https://guard.aikido.dev/";
        };
    }
    
    public static String getAikidoRealtimeEndpoint() {
        String endpoint = System.getenv("AIKIDO_REALTIME_ENDPOINT");
        if (endpoint != null && !endpoint.isEmpty()) {
            if (!endpoint.endsWith("/")) {
                return endpoint + "/"; // Make sure there is a trailing slash
            }
            return endpoint;
        }

        // Default option :
        return "https://runtime.aikido.dev/";
    }
}
