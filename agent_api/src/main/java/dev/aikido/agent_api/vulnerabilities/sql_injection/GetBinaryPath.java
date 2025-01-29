package dev.aikido.agent_api.vulnerabilities.sql_injection;

public final class GetBinaryPath {
    private GetBinaryPath() {}

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
        }

        if (os.contains("win")) {
            fileName.append("pc-windows-gnu.dll"); // Windows
        } else if (os.contains("mac")) {
            fileName.append("apple-darwin.dylib"); // macOS
        } else if (os.contains("nix") || os.contains("nux")) {
            fileName.append("unknown-linux-gnu.so"); // Linux
        }
        return fileName.toString();
    }
}
