package vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.sql_injection.SqlDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlInjectionTest {

    @Test
    public void testShouldReturnEarly() {
        // Test cases where the function should return True

        // User input is empty
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", ""));

        // User input is a single character
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "a"));

        // User input is larger than query
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "SELECT * FROM users WHERE id = 1"));

        // User input not in query
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "DELETE"));

        // User input is alphanumerical
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users123", "users123"));
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users_123", "users_123"));
        assertTrue(SqlDetector.shouldReturnEarly("SELECT __1 FROM users_123", "__1"));
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM table_name_is_fun_12", "table_name_is_fun_12"));

        // User input is a valid comma-separated number list
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "1,2,3"));

        // User input is a valid number
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "123"));

        // User input is a valid number with spaces
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "  123  "));

        // User input is a valid number with commas
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "1, 2, 3"));

        // Test cases where the function should return False

        // User input is in query
        assertFalse(SqlDetector.shouldReturnEarly("SELECT * FROM users", " users"));

        // User input is a valid string in query
        assertFalse(SqlDetector.shouldReturnEarly("SELECT * FROM users", "SELECT "));

        // User input is a valid string in query with special characters
        assertFalse(SqlDetector.shouldReturnEarly("SELECT * FROM users; DROP TABLE", "users; DROP TABLE"));
    }
}
