package vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.sql_injection.Dialect;
import dev.aikido.agent_api.vulnerabilities.sql_injection.RustSQLInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RustSQLInterfaceTest {
    @Test
    public void testItWorks() {
        boolean injectionResult = RustSQLInterface.detectSqlInjection("SELECT * FROM table;", "table;", new Dialect("postgres"));
        assertTrue(injectionResult);
        injectionResult = RustSQLInterface.detectSqlInjection("SELECT * FROM table;", "table", new Dialect("postgres"));
        assertFalse(injectionResult);
    }
}
