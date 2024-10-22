package dev.aikido.AikidoAgent.vulnerabilities.path_traversal;

import dev.aikido.AikidoAgent.vulnerabilities.AikidoException;
import dev.aikido.AikidoAgent.vulnerabilities.sql_injection.Dialect;

public class PathTraversalException extends AikidoException {
    public PathTraversalException(String msg) {
        super(msg);
    }

    public static PathTraversalException get() {
        String defaultMsg = generateDefaultMessage("Path Traversal");
        return new PathTraversalException(defaultMsg);
    }
}
