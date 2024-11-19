package dev.aikido.agent_api.collectors;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class RedirectCollector {
    public static void report(URL url, Map<String, List<String>> headers, int statusCode) {
        List<String> locationHeaders = headers.getOrDefault("location", List.of());
        if (!locationHeaders.isEmpty()) {
            System.out.println(url + " With status code: " + statusCode + "\n|-> Redirects to : "+ headers);
        }

    }
}
