package dev.aikido.AikidoAgent.background.utilities;

import java.nio.file.Path;

public class UDSPath {
    public static Path getUDSPath() {
        return Path
            .of(System.getProperty("user.home"))
            .resolve("aikido2342.socket");
    }
}
