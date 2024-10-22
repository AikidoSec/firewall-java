package dev.aikido.AikidoAgent.helpers.env;

public class Endpoints {
    public static String getAikidoAPIEndpoint() {
        String endpoint = System.getenv("AIKIDO_ENDPOINT");
        if (endpoint != null && !endpoint.isEmpty()) {
            if (!endpoint.endsWith("/")) {
                return endpoint + "/"; // Make sure there is a trailing slash
            }
            return endpoint;
        }

        // Default option :
        return "https://guard.aikido.dev/";
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
