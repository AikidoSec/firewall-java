package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDBCConnectionAdviceTest {
    private Connection connection;
    private Method prepareStatement;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db", "user", "password");
        prepareStatement = connection.getClass().getMethod("prepareStatement", String.class);
        Context.set(null);
        StatisticsStore.clear();
        ServiceConfigStore.updateBlocking(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
        Context.set(null);
        StatisticsStore.clear();
        ServiceConfigStore.updateBlocking(true);
        JDBCConnectionAdvice.jdbcCallStack.remove();
    }

    @Test
    void reportsNestedCallsWithDifferentSql() throws Throwable {
        String outerSql = "SELECT 1";
        String nestedSql = "SELECT 2";

        String outerCall = JDBCConnectionAdvice.before(connection, prepareStatement, outerSql);
        try {
            String nestedCall = JDBCConnectionAdvice.before(connection, prepareStatement, nestedSql);
            JDBCConnectionAdvice.after(nestedCall);
        } finally {
            JDBCConnectionAdvice.after(outerCall);
        }

        var operation = StatisticsStore.getStatsRecord().operations()
                .get("(PostgreSQL JDBC Driver) java.sql.Connection.prepareStatement");
        assertNotNull(operation);
        assertEquals(2, operation.total());
        assertTrue(JDBCConnectionAdvice.jdbcCallStack.get().isEmpty());
    }
}
