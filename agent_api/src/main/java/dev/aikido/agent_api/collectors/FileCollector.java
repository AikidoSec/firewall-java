package dev.aikido.agent_api.collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileCollector {
    private static final Logger logger = LogManager.getLogger(FileCollector.class);
    public static void report(Object filePath) {
        logger.debug("New file path: {}", filePath);
        if (filePath == null) {
            return; // Make sure filePath is defined
        }
        logger.debug("New file path: {}", filePath);
    }
}
