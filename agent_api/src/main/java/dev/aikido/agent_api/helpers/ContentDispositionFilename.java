package dev.aikido.agent_api.helpers;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.Optional;

public final class ContentDispositionFilename {
    private ContentDispositionFilename() {}
    private static final Logger logger = LogManager.getLogger(ContentDispositionFilename.class);

    public static Optional<String> extract(String contentDisposition) {
        try {
            ContentDispositionHeader.ParseResult res = ContentDispositionHeader.parse(contentDisposition);

            if (res.params() == null) {
                return Optional.empty();
            }

            // filename* is preferred over filename;
            if (!res.params().getOrDefault("filename*", "").isEmpty()) {
                return Optional.of(res.params().get("filename*"));
            }

            if (!res.params().getOrDefault("filename", "").isEmpty()) {
                return Optional.of(res.params().get("filename"));
            }
        } catch(RuntimeException e) {
            logger.debug("Error while parsing content disposition header for filename: %s", e.getMessage());
        }

        return Optional.empty();
    }

}
