package dev.aikido.agent_api.vulnerabilities.path_traversal;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUrlParser {
    private FileUrlParser() {}
    public static String parseAsFileUrl(String path) {
        try {
            Path filePath = null;

            if (path.startsWith("file:")) {
                URI uri = new URI(path);
                filePath = Paths.get(uri.getPath());
            } else{
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                URI fileUri = new URI("file", "", path, null);
                filePath = Paths.get(fileUri.getPath());
            }
            Path normalizedPath = filePath.normalize();
            return normalizedPath.toString();
        } catch (Exception ignored) {
            return null;
        }
    }
}
