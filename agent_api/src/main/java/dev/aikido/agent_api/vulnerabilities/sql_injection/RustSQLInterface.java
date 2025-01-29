package dev.aikido.agent_api.vulnerabilities.sql_injection;

import static dev.aikido.agent_api.vulnerabilities.sql_injection.GetBinaryPath.getPathForBinary;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import jnr.ffi.LibraryLoader;
import jnr.ffi.LibraryOption;

public final class RustSQLInterface {
    private RustSQLInterface() {}

    private static final Logger logger = LogManager.getLogger(RustSQLInterface.class);

    public interface SqlLib {
        int detect_sql_injection(String query, String userinput, int dialect);
    }

    public static boolean detectSqlInjection(String query, String userInput, Dialect dialect) {
        int dialectInteger = dialect.getDialectInteger();
        try {
            SqlLib lib = loadLibrary();
            if (lib != null) {
                return lib.detect_sql_injection(query, userInput, dialectInteger) != 0;
            }
        } catch (Exception e) {
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
        SqlLib library = LibraryLoader.loadLibrary(SqlLib.class, libraryOptions, path);
        if (library == null) {
            logger.error("Failed to load zen binaries.");
        }
        return library;
    }
}
