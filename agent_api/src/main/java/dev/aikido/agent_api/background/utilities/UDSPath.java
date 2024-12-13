package dev.aikido.agent_api.background.utilities;

import dev.aikido.agent_api.helpers.env.TemporaryDir;
import dev.aikido.agent_api.helpers.env.Token;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UDSPath {
    private UDSPath() {}
    private static final String prefix = "aikido_java";
    public static File getQueueDir(Token token) throws IOException {
        String tempName = String.format("%s_%s", prefix, token.hash());
        return Files.createTempDirectory(tempName).toFile();
    }
}
