package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.sql_injection.SQLInjectionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class MariadbWrapperTest {
    private Connection connection;

    @BeforeAll
    public static void clean() {
        Context.set(null);
        ServiceConfigStore.updateBlocking(true);
    }

    @BeforeEach
    public void setUp() throws SQLException {
        // Connect to the MySQL database
        connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/db?allowPublicKeyRetrieval=true&useSSL=false", "user", "password");
        StatisticsStore.clear();
        ServiceConfigStore.updateBlocking(true);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        Context.set(null);
        StatisticsStore.clear();
        ServiceConfigStore.updateBlocking(true);
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
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: MySQL", exception.getMessage());
    }

    @Test
    public void testPrepareStatementReportsOnceInDetectionOnlyMode() throws SQLException {
        String payload = "Malicious Pet', 'Gru from the Minions') -- ";
        String sql = "INSERT INTO pets (pet_name, owner) VALUES ('" + payload + "', 'Aikido Security')";
        Context.set(new EmptySampleContextObject(payload));
        ServiceConfigStore.updateBlocking(false);

        assertDoesNotThrow(() -> connection.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT));

        var stats = StatisticsStore.getStatsRecord();
        assertEquals(1, stats.requests().attacksDetected().total());
        assertEquals(1, stats.operations().values().stream()
                .filter(record -> record.getKind() == OperationKind.SQL_OP)
                .count());
        var operation = stats.operations()
                .get("(MariaDB Connector/J) java.sql.Connection.prepareStatement");
        assertNotNull(operation);
        assertEquals(1, operation.total());
        assertEquals(1, operation.getAttacksDetected().get("total"));
        assertEquals(0, operation.getAttacksDetected().get("blocked"));
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
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: MySQL", exception.getMessage());
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
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: MySQL", exception.getMessage());
    }

    @Test
    public void testAddBatch() throws SQLException {
        Statement stmt = connection.createStatement();
        Context.set(new EmptySampleContextObject("Fluffy"));

        // Valid batch
        assertDoesNotThrow(() -> {
            stmt.addBatch("INSERT INTO pets (pet_name, owner) VALUES (\"Fluffy\", \"test\");");
            stmt.executeBatch();
        });

        // Invalid batch (SQL Injection)
        Context.set(new EmptySampleContextObject("\"Fluffy2\", \"test\""));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            stmt.addBatch("INSERT INTO pets (pet_name, owner) VALUES (\"Fluffy2\", \"test\");");
            stmt.executeBatch();
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: MySQL", exception.getMessage());
    }

    @Test
    public void testExecuteLargeUpdate() throws SQLException {
        Statement stmt = connection.createStatement();
        Context.set(new EmptySampleContextObject("Buddy"));

        // Valid update
        assertDoesNotThrow(() -> {
            stmt.executeLargeUpdate("UPDATE pets SET pet_name = \"Buddy\" WHERE pet_name = \"Fluffy\";");
        });

        // Invalid update (SQL Injection)
        Context.set(new EmptySampleContextObject("pet_name = \"Fluffy2\""));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            stmt.executeLargeUpdate("UPDATE pets SET pet_name = \"Buddy2\" WHERE pet_name = \"Fluffy2\";");
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: MySQL", exception.getMessage());
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
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: MySQL", exception.getMessage());
    }

    @Test
    public void testExecuteUpdate() throws SQLException {
        Statement stmt = connection.createStatement();
        Context.set(new EmptySampleContextObject("UPDATE"));

        // Valid update
        assertDoesNotThrow(() -> {
            int rowsAffected = stmt.executeUpdate("UPDATE pets SET pet_name = \"Buddy\" WHERE pet_name = \"Fluffy\";");
            assertTrue(rowsAffected >= 0); // Ensure that the update was successful
        });

        // Invalid update (SQL Injection)
        Context.set(new EmptySampleContextObject("pet_name = \"Fluffy2\""));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            stmt.executeUpdate("UPDATE pets SET pet_name = \"Buddy2\" WHERE pet_name = \"Fluffy2\";");
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: MySQL", exception.getMessage());
    }
}
