package dev.aikido.agent_api.collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

public class URLCollector {
    private static final Logger logger = LogManager.getLogger(URLCollector.class);
    public static void report(URL url) {
        logger.debug("URL Reported, with port: {}; Hostname: {}", url.getPort(), url.getHost());
    }
}
