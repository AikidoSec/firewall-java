package dev.aikido.agent_api.vulnerabilities.sql_injection;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

public final class GetBinaryPath {
    private GetBinaryPath() {}
    private static final Logger logger = LogManager.getLogger(GetBinaryPath.class);

    public static String getPathForBinary() {
      String fileName = getFileName();
      String aikidoDirectory = System.getProperty("AIK_agent_dir");
      if (aikidoDirectory == null) {
          return null;
      }
      return aikidoDirectory + "/binaries/" + fileName;
    }
    private static String getFileName() {
        String os = System.getProperty("os.name").toLowerCase();
        String architecture = System.getProperty("os.arch").toLowerCase();
        StringBuilder fileName = new StringBuilder();
        System.err.println(os);
        System.err.println(architecture);

        fileName.append("libzen_internals_"); // Start of file

        if (architecture.contains("aarch64")) {
            fileName.append("aarch64-"); // Add architecture to file name
        } else if (architecture.contains("64")) {
            fileName.append("x86_64-"); // Add architecture to file name
        } else {
            fileName.append("x86_64-"); // Default to x86-64
        }

        boolean isMusl = os.contains("musl") || architecture.contains("musl");

        if (os.contains("win")) {
            fileName.append("pc-windows-gnu.dll"); // Windows
        } else if (os.contains("mac")) {
            fileName.append("apple-darwin.dylib"); // macOS
        } else { // os.contains("nix") || os.contains("nux")
            // Default to linux
            fileName.append(String.format("unknown-linux-%s.so", getLibCVariant()));
        }
        return fileName.toString();
    }

    private static String getLibCVariant() {
        try {
            Process process = new ProcessBuilder("uname", "-o").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                if (line.toLowerCase().contains("gnu")) {
                    return "gnu";
                } else {
                    return "musl";
                }
            }
        } catch (IOException e) {
            logger.trace(e);
        }
        return "gnu"; // Default to gnu
    }
}
