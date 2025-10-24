package dev.aikido.agent_api.vulnerabilities.sql_injection;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import jnr.ffi.Library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.annotation.Native;

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

        fileName.append("libzen_internals_"); // Start of file

        if (architecture.contains("aarch64")) {
            fileName.append("aarch64-"); // Add architecture to file name
        } else if (architecture.contains("64")) {
            fileName.append("x86_64-"); // Add architecture to file name
        } else {
            fileName.append("x86_64-"); // Default to x86-64
        }

        if (os.contains("win")) {
            fileName.append("pc-windows-gnu.dll"); // Windows
        } else if (os.contains("mac")) {
            fileName.append("apple-darwin.dylib"); // macOS
        } else {
            // Default to linux
            fileName.append(String.format("unknown-linux-%s.so", getLibCVariant()));
        }
        return fileName.toString();
    }

    private static String getLibCVariant() {
        try {
            // ldd --version, if supported, returns something like `musl libc (aarch64)` for musl
            // or `ldd (Ubuntu GLIBC 2.39-0ubuntu8.6) 2.39` for GNU
            Process process = Runtime.getRuntime().exec("ldd --version");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                logger.error(line);
                if (line.toLowerCase().contains("musl")) {
                    return "musl";
                } else if (line.toLowerCase().contains("gnu") || line.toLowerCase().contains("glibc")) {
                    return "gnu";
                }
            }
        } catch (IOException e) {
            logger.debug(e);
        }
        return "gnu";
    }
}
