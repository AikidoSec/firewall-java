package dev.aikido.agent_api.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContentDispositionFilename {
    private ContentDispositionFilename() {}

    public static String extractFilenameFromHeader(String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isEmpty()) {
            return null;
        }

        // Regex to match the filename in the Content-Disposition header
        String regex = "filename[\\s]*=[\\s]*\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(contentDisposition);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Fallback for cases where the filename is not quoted
        regex = "filename[\\s]*=[\\s]*([^;\\s]+)";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(contentDisposition);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

}
