package vulnerabilities.sql_injection;

import dev.aikido.agent_api.vulnerabilities.sql_injection.Dialect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DialectTest {

    @Test
    public void testPostgreSQLDialect() {
        Dialect dialect = new Dialect("postgresql");
        assertEquals(9, dialect.getDialectInteger());
        assertEquals("PostgreSQL", dialect.getHumanName());
    }

    @Test
    public void testMySQLDialect() {
        Dialect dialect = new Dialect("mysql");
        assertEquals(8, dialect.getDialectInteger());
        assertEquals("MySQL", dialect.getHumanName());
    }

    @Test
    public void testMicrosoftSQLServerDialect() {
        Dialect dialect = new Dialect("microsoft sql server");
        assertEquals(7, dialect.getDialectInteger());
        assertEquals("Microsoft SQL", dialect.getHumanName());
    }

    @Test
    public void testGenericDialect() {
        Dialect dialect = new Dialect("unknown");
        assertEquals(0, dialect.getDialectInteger());
        assertEquals("Generic", dialect.getHumanName());
    }

    @Test
    public void testNullDialect() {
        Dialect dialect = new Dialect(null);
        assertEquals(0, dialect.getDialectInteger());
        assertEquals("Generic", dialect.getHumanName());
    }
}
