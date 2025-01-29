package vulnerabilities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aikido.agent_api.vulnerabilities.sql_injection.Dialect;
import dev.aikido.agent_api.vulnerabilities.sql_injection.RustSQLInterface;
import org.junit.jupiter.api.Test;

public class RustSQLInterfaceTest {
    @Test
    public void testItWorks() {
        boolean injectionResult =
                RustSQLInterface.detectSqlInjection("SELECT * FROM table;", "table;", new Dialect("postgresql"));
        assertTrue(injectionResult);
        injectionResult =
                RustSQLInterface.detectSqlInjection("SELECT * FROM table;", "table", new Dialect("postgresql"));
        assertFalse(injectionResult);
    }
}
