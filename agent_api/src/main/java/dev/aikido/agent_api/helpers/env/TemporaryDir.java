package dev.aikido.agent_api.helpers.env;

import java.nio.file.Path;

public class TemporaryDir {
    public static String getTemporaryDir() {
        // Read out environment variable :
        String tempDirEnv = System.getenv("AIKIDO_TMP_DIR");
        if (tempDirEnv != null && !tempDirEnv.isEmpty()) {
            return tempDirEnv;
        }
        // Default :
        return Path
            .of(System.getProperty("user.home"))
            .resolve(".tmp")
            .toString();
    }
}
