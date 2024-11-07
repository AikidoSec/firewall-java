package dev.aikido.agent_api.helpers.env;

public class AikidoDir {
    public static String getAikidoDir() {
        // Read out environment variable :
        String aikidoDirectoryEnv = System.getenv("AIKIDO_DIRECTORY");
        if (aikidoDirectoryEnv != null && !aikidoDirectoryEnv.isEmpty()) {
            if (!aikidoDirectoryEnv.endsWith("/")) {
                return aikidoDirectoryEnv + "/"; // Make sure there is a trailing slash
            }
            return aikidoDirectoryEnv;
        }
        return null;
    }
}
