package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import dev.aikido.agent_api.vulnerabilities.sql_injection.SQLInjectionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PostgresWrapperTest {
    private Connection connection;
    public static class SampleContextObject extends ContextObject {
        public SampleContextObject(String argument) {
            this.method = "GET";
            this.source = "web";
            this.url = "https://example.com/api/resource";
            this.route = "/api/resource";
            this.remoteAddress = "192.168.1.1";
            this.headers = new HashMap<>();
            this.query = new HashMap<>();
            this.query.put("sql1", new String[]{argument});
            this.cookies = new HashMap<>();
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
        }
    }
    @BeforeAll
    public static void clean() {
        Context.set(null);
        ThreadCache.set(null);
    }
    @BeforeEach
    public void setUp() throws SQLException {
        // Connect to the PostgreSQL database
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db", "user", "password");
        ThreadCache.set(new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes()));
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        Context.set(null);
        ThreadCache.set(null);
    }
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    public void testSelectSqlWithPrepareStatement() throws SQLException {
        ThreadCache.set(null);

        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        Context.set(new SampleContextObject("SELECT * FROM notpets;"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        Context.set(new SampleContextObject("* FROM pets"));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL",  exception.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSelectSqlSafeWithPrepareStatement() throws SQLException {
        ThreadCache.set(null);

        Context.set(new SampleContextObject("FROM"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        Context.set(new SampleContextObject("pets"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
        Context.set(new SampleContextObject("SELECT *"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT * FROM pets;").executeQuery();
        });
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    public void testSelectSqlWithPreparedStatementWithoutExecute() throws SQLException {
        ThreadCache.set(null);

        Context.set(new SampleContextObject("SELECT * FROM notpets;"));
        assertDoesNotThrow(() -> {
            connection.prepareStatement("SELECT pet_name FROM pets;");
        });

        Context.set(new SampleContextObject("* FROM pets"));
        Exception exception = assertThrows(SQLInjectionException.class, () -> {
            connection.prepareStatement("SELECT * FROM pets;");
        });
        assertEquals("Aikido Zen has blocked SQL Injection, Dialect: PostgreSQL",  exception.getMessage());
    }
}
