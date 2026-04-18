package vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.sql_injection.Dialect;
import dev.aikido.agent_api.vulnerabilities.sql_injection.RustSQLInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RustSQLInterfaceTest {
    @Test
    public void testItWorks() {
        int injectionResult = RustSQLInterface.detectSqlInjection("SELECT * FROM table;", "table;", new Dialect("postgresql"));
        assertEquals(1, injectionResult);
        injectionResult = RustSQLInterface.detectSqlInjection("SELECT * FROM table;", "table", new Dialect("postgresql"));
        assertEquals(0, injectionResult);
    }
}
