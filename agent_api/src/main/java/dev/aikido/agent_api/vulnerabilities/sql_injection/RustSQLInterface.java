package dev.aikido.agent_api.vulnerabilities.sql_injection;

import jnr.ffi.LibraryLoader;
import jnr.ffi.LibraryOption;
import jnr.ffi.annotations.Encoding;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

import static dev.aikido.agent_api.vulnerabilities.sql_injection.GetBinaryPath.getPathForBinary;

public final class RustSQLInterface {
    private RustSQLInterface() {}

    private static final Logger logger = LogManager.getLogger(RustSQLInterface.class);

    @Encoding("UTF-8")
    public interface SqlLib {
        int detect_sql_injection(String query, long queryLen, String userinput, long userinputLen, int dialect);
    }

    public static boolean detectSqlInjection(String query, String userInput, Dialect dialect) {
        int dialectInteger = dialect.getDialectInteger();
        try {
            SqlLib lib = loadLibrary();
            if (lib != null) {
                long queryLen = query != null ? query.getBytes(StandardCharsets.UTF_8).length : 0;
                long userInputLen = userInput != null ? userInput.getBytes(StandardCharsets.UTF_8).length : 0;
                int result = lib.detect_sql_injection(query, queryLen, userInput, userInputLen, dialectInteger);
                return result == 1;
            }
        } catch (Throwable e) {
            logger.trace(e);
        }
        return false;
    }
    public static SqlLib loadLibrary() {
        String path = getPathForBinary();
        if (path == null || !Files.exists(Path.of(path))) {
            logger.error("Could not load zen binaries used for SQL Injection algorithm. Path: %s", path);
            return null;
        }
        Map<LibraryOption, Object> libraryOptions = new HashMap<>();
        libraryOptions.put(LibraryOption.LoadNow, true); // load immediately instead of lazily (ie on first use)
        libraryOptions.put(LibraryOption.IgnoreError, true); // calls shouldn't save last errno after call

        SqlLib library = null;
        try {
            library = LibraryLoader.loadLibrary(SqlLib.class, libraryOptions, path);
        } catch (Throwable e) {
            String os = System.getProperty("os.name").toLowerCase();
            String architecture = System.getProperty("os.arch").toLowerCase();
            logger.error("Failed to load Zen Internals (%s, %s)", os, architecture);
            throw e;
        }

        if (library == null) {
            logger.error("Failed to load zen binaries.");
        }
        return library;
    }
}
