package dev.aikido.AikidoAgent.background.utilities;

import dev.aikido.AikidoAgent.helpers.env.TemporaryDir;
import dev.aikido.AikidoAgent.helpers.env.Token;

import java.nio.file.Path;

public class UDSPath {
    private static final String prefix = "aikido_java";
    public static Path getUDSPath(Token token) {
        String temporaryDir = TemporaryDir.getTemporaryDir();
        String hash = token.hash();
        return Path.of(String.format("%s/%s_%s.sock", temporaryDir, prefix, hash));
    }
}
