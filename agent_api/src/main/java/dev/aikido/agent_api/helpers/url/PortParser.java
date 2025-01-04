package dev.aikido.agent_api.helpers.url;

import java.net.URL;

public class PortParser {
    public static int getPortFromURL(URL url) {
        if (url.getPort() != -1) {
            return url.getPort();
        }
        if (url.getProtocol().equals("https")) {
            return 443; // Default port for HTTPS traffic
        }
        if (url.getProtocol().equals("http")) {
            return 80; // Default port for HTTP traffic
        }
        return -1; // Port not found
    }
}
