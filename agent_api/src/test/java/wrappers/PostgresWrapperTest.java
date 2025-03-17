package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ConfigStore;
import dev.aikido.agent_api.vulnerabilities.sql_injection.SQLInjectionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class PostgresWrapperTest {
    private Connection connection;

    @BeforeAll
    public static void clean() {
        Context.set(null);
    }
    @BeforeEach
    public void setUp() throws SQLException {
        // Connect to the PostgreSQL database
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db", "user", "password");
        ConfigStore.updateBlocking(true);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        Context.set(null);
    }

    @Test
    public void testSelectSqlWithPrepareStatement() throws SQLException {
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        Context.set(new EmptySampleContextObject("SELECT * FROM notpets;"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        Context.set(new EmptySampleContextObject("* FROM pets"));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL",  exception.getMessage());
    }

    @Test
    public void testSelectSqlSafeWithPrepareStatement() throws SQLException {
        Context.set(new EmptySampleContextObject("FROM"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        Context.set(new EmptySampleContextObject("pets"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        Context.set(new EmptySampleContextObject("SELECT *"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
    }

    @Test
    public void testSelectSqlWithPreparedStatementWithoutExecute() throws SQLException {
        Context.set(new EmptySampleContextObject("SELECT * FROM notpets;"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT pet_name FROM pets;");
        });

        Context.set(new EmptySampleContextObject("* FROM pets"));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            connection.prepareStatement("SELECT * FROM pets;");
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL",  exception.getMessage());
    }

    @Test
    public void testExecute() throws SQLException {
        Statement stmt = connection.createStatement();
        Context.set(new EmptySampleContextObject("SELECT * FROM notpets;"));

        // Valid query
        assertDoesNotThrow(() -> {
            stmt.execute("SELECT pet_name FROM pets;");
        });

        // Invalid query (SQL Injection)
        Context.set(new EmptySampleContextObject("* FROM pets"));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            stmt.execute("SELECT * FROM pets;");
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL", exception.getMessage());
    }

    @Test
    public void testAddBatch() throws SQLException {
        Statement stmt = connection.createStatement();
        Context.set(new EmptySampleContextObject("Fluffy"));

        // Valid batch
        assertDoesNotThrow(() -> {
            stmt.addBatch("INSERT INTO pets (pet_name, owner) VALUES ('Fluffy', 'test');");
            stmt.executeBatch();
        });

        // Invalid batch (SQL Injection)
        Context.set(new EmptySampleContextObject("'Fluffy2', 'test'"));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            stmt.addBatch("INSERT INTO pets (pet_name, owner) VALUES ('Fluffy2', 'test');");
            stmt.executeBatch();
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL", exception.getMessage());
    }

    @Test
    public void testExecuteLargeUpdate() throws SQLException {
        Statement stmt = connection.createStatement();
        Context.set(new EmptySampleContextObject("Buddy"));

        // Valid update
        assertDoesNotThrow(() -> {
            stmt.executeLargeUpdate("UPDATE pets SET pet_name = 'Buddy' WHERE pet_name = 'Fluffy';");
        });

        // Invalid update (SQL Injection)
        Context.set(new EmptySampleContextObject("pet_name = 'Fluffy2'"));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            stmt.executeLargeUpdate("UPDATE pets SET pet_name = 'Buddy2' WHERE pet_name = 'Fluffy2';");
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL", exception.getMessage());
    }

    @Test
    public void testExecuteQuery() throws SQLException {
        Statement stmt = connection.createStatement();
        Context.set(new EmptySampleContextObject("* FROM pets"));

        // Valid query
        assertDoesNotThrow(() -> {
            stmt.executeQuery("SELECT pet_name FROM pets;");
        });

        // Invalid query (SQL Injection)
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            stmt.executeQuery("SELECT * FROM pets;");
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL", exception.getMessage());
    }

    @Test
    public void testExecuteUpdate() throws SQLException {
        Statement stmt = connection.createStatement();
        Context.set(new EmptySampleContextObject("SELECT * FROM notpets;"));

        // Valid update
        assertDoesNotThrow(() -> {
            int rowsAffected = stmt.executeUpdate("UPDATE pets SET pet_name = 'Buddy' WHERE pet_name = 'Fluffy';");
            assertTrue(rowsAffected >= 0); // Ensure that the update was successful
        });

        // Invalid update (SQL Injection)
        Context.set(new EmptySampleContextObject("* FROM pets"));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            stmt.executeUpdate("SELECT * FROM pets;");
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL", exception.getMessage());
    }

}
