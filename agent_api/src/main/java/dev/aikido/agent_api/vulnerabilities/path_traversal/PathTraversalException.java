package dev.aikido.agent_api.vulnerabilities.path_traversal;

import dev.aikido.agent_api.vulnerabilities.AikidoException;

public class PathTraversalException extends AikidoException {
    public PathTraversalException(String msg) {
        super(msg);
    }

    public static PathTraversalException get() {
        String defaultMsg = generateDefaultMessage("Path Traversal");
        return new PathTraversalException(defaultMsg);
    }
}
