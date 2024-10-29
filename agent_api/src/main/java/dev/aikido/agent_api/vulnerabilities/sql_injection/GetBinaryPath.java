package dev.aikido.agent_api.vulnerabilities.sql_injection;

import static dev.aikido.agent_api.helpers.env.AikidoDir.getAikidoDir;

public class GetBinaryPath {
    public static String getPathForBinary() {
      String fileName = getFileName();
      if (fileName == null) {
          return null;
      }
      String aikidoDirectory = getAikidoDir();
      if (aikidoDirectory == null) {
          return null;
      }
      return aikidoDirectory + "binaries/" + fileName;
    }
    private static String getFileName() {
        String os = System.getProperty("os.name").toLowerCase();
        String architecture = System.getProperty("os.arch").toLowerCase();

        if (os.contains("win")) {
            // Windows
            if (architecture.contains("64")) {
                return "libzen_internals_x86_64-pc-windows-gnu.dll";
            }
        } else if (os.contains("mac")) {
            // macOS
            if (architecture.contains("aarch64")) {
                return "libzen_internals_aarch64-apple-darwin.dylib";
            } else if (architecture.contains("x86_64")) {
                return "libzen_internals_x86_64-apple-darwin.dylib";
            }
        } else if (os.contains("nix") || os.contains("nux")) {
            // Linux
            if (architecture.contains("aarch64")) {
                return "libzen_internals_aarch64-unknown-linux-gnu.so";
            } else if (architecture.contains("x86_64")) {
                return "libzen_internals_x86_64-unknown-linux-gnu.so";
            }
        }
        return null;
    }
}
