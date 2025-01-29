package dev.aikido.agent_api.background.utilities;

import dev.aikido.agent_api.helpers.env.TemporaryDir;
import dev.aikido.agent_api.helpers.env.Token;

import java.io.File;
import java.nio.file.Path;

public final class UDSPath {
    private UDSPath() {}
    private static final String prefix = "aikido_java";
    public static File getUDSPath(Token token) {
        String temporaryDir = TemporaryDir.getTemporaryDir();
        String hash = token.hash();
        return Path.of(String.format("%s/%s_%s.sock", temporaryDir, prefix, hash)).toFile();
    }
}
