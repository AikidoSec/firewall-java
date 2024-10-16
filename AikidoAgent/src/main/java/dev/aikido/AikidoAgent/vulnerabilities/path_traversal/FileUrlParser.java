package dev.aikido.AikidoAgent.vulnerabilities.path_traversal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUrlParser {

    public static String parseAsFileUrl(String path) throws URISyntaxException {
        Path filePath;

        if (path.startsWith("file:")) {
            URI uri = new URI(path);
            filePath = Paths.get(uri.getPath());
        } else {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            URI fileUri = new URI("file", "", path, null);
            filePath = Paths.get(fileUri.getPath());
        }

        Path normalizedPath = filePath.normalize();
        return normalizedPath.toString();
    }
}
