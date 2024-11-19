package dev.aikido.agent_api.collectors;


import java.net.URL;
import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.helpers.url.PortParser.getPortFromURL;

public class RedirectCollector {
    public record RedirectEntry(String hostname, int port) {};
    public static void report(URL origin, URL dest) {
        RedirectEntry originEntry = new RedirectEntry(origin.getHost(), getPortFromURL(origin));
        RedirectEntry destEntry = new RedirectEntry(dest.getHost(), getPortFromURL(dest));

        System.out.println(originEntry + " ↦ " + destEntry);
    }
}
