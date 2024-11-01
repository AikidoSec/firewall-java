package dev.aikido.agent_api.vulnerabilities.sql_injection;

import jnr.ffi.LibraryLoader;
import jnr.ffi.LibraryOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RustSQLInterface {
    private static final Logger logger = LogManager.getLogger(RustSQLInterface.class);
    public interface SqlLib {
        int detect_sql_injection(String query, String userinput, int dialect);
    }
    public static boolean detectSqlInjection(String query, String userInput, Dialect dialect) {
        int dialectInteger = dialect.getDialectInteger();
        try {
            SqlLib lib = loadLibrary();
            return lib.detect_sql_injection(query, userInput, dialectInteger) != 0;
        } catch (URISyntaxException e) {
            logger.trace(e);
            return false;
        }
    }
    private static SqlLib loadLibrary() throws URISyntaxException {
        String path = getBinaryPath();
        Map<LibraryOption, Object> libraryOptions = new HashMap<>();
        libraryOptions.put(LibraryOption.LoadNow, true); // load immediately instead of lazily (ie on first use)
        libraryOptions.put(LibraryOption.IgnoreError, true); // calls shouldn't save last errno after call
        return LibraryLoader.loadLibrary(SqlLib.class, libraryOptions, path);
    }
    private static String getBinaryPath() throws URISyntaxException {
        URL res = RustSQLInterface.class.getClassLoader().getResource("libzen_internals_x86_64-unknown-linux-gnu.so");
        if (res != null) {
            File file = Paths.get(res.toURI()).toFile();
            return file.getAbsolutePath();
        }
        return "";
    }
}
