package dev.aikido.agent_api.helpers;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContentDispositionFilename {
    private ContentDispositionFilename() {}

    public static Optional<String> extractFilenameFromHeader(String contentDisposition) {
        ContentDispositionHeader.ParseResult res = ContentDispositionHeader.parse(contentDisposition);

        if (res.params() == null) {
            return Optional.empty();
        }

        return Optional.empty();
    }

}
