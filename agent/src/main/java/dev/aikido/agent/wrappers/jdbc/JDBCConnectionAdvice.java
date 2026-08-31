package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent_api.collectors.SQLCollector;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;
import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.Deque;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

public final class JDBCConnectionAdvice {
    public static final Logger logger = LogManager.getLogger(JDBCConnectionAdvice.class);

    // Database drivers often delegate one prepareStatement overload to another.
    // Skip only nested calls with the same SQL to avoid repeated WASM checks and duplicate detection-only events.
    //
    // Example for sql1 = "SELECT 1", where both overloads receive the same sql1 object:
    // prepareStatement(sql1) -> prepareStatement(sql1, options)
    // Enter outer: [] -> [sql1] (check)
    // Enter inner: [sql1] -> [sql1, sql1] (skip)
    // Exit inner: [sql1, sql1] -> [sql1]
    // Exit outer: [sql1] -> []
    public static final ThreadLocal<Deque<String>> jdbcCallStack = ThreadLocal.withInitial(ArrayDeque::new);
    private JDBCConnectionAdvice() {}
    public static ElementMatcher<? super MethodDescription> getMatcher(String module) {
        ElementMatcher.Junction<? super MethodDescription> statementMatcher =
                isDeclaredBy(nameContainsIgnoreCase(module).and(isSubTypeOf(Statement.class)))
                .and(
                    named("addBatch").or(named("execute"))
                    .or(named("executeLargeUpdate")).or(named("executeQuery"))
                    .or(named("executeUpdate"))
                ).and(ElementMatchers.takesArgument(0, String.class));
        ElementMatcher.Junction<? super MethodDescription> connectionMatcher =
                isDeclaredBy(nameContainsIgnoreCase(module).and(isSubTypeOf(Connection.class)))
                .and(
                    named("prepareStatement").or(named("prepareCall")).or(named("nativeSQL"))
                );
        return statementMatcher.or(connectionMatcher);
    }

    /* Wraps all functions accepting sql for the Connection interface and for the Statement interface :
     * -> prepareStatement(sql, [...]), prepareCall(sql, [...]), nativeSQL(sql)
     * addBatch(sql), execute(sql, [...]), executeLargeUpdate(sql, [...]), executeQuery(sql), executeUpdate(sql, [...])
     */
    @Advice.OnMethodEnter
    public static String before(
            @Advice.This(typing = DYNAMIC, optional = true) Object obj,
            @Advice.Origin Executable method,
            @Advice.Argument(0) String sql
    ) throws Throwable {
        if (sql == null) {
            return null;
        }
        Deque<String> sqlCalls = jdbcCallStack.get();
        boolean isDelegatedCall = !sqlCalls.isEmpty() && sqlCalls.peek() == sql;
        sqlCalls.push(sql);
        if (!isDelegatedCall) {
            try {
                // Get connection whether it's from a Statement or not:
                Connection databaseConnection = null;
                String methodName = method.getName();
                if (obj instanceof Connection objConnection) {
                    databaseConnection = objConnection;
                    methodName = "Connection." + methodName;
                } else if(obj instanceof Statement objStatement) {
                    databaseConnection = objStatement.getConnection();
                    methodName = "Statement." + methodName;
                }
                DatabaseMetaData metaData = databaseConnection.getMetaData();
                String operation = "(" + metaData.getDriverName() + ") java.sql." + methodName;
                String dialect = metaData.getDatabaseProductName().toLowerCase();
                SQLCollector.report(sql, dialect, operation);
            } catch (AikidoException e) {
                sqlCalls.pop();
                throw e;
            } catch (Throwable e) {
                logger.debug(e);
            }
        }
        return sql;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void after(@Advice.Enter String enteredSql) {
        if (enteredSql == null) {
            return;
        }
        Deque<String> sqlCalls = jdbcCallStack.get();
        if (!sqlCalls.isEmpty() && sqlCalls.peek() == enteredSql) {
            sqlCalls.pop();
        }
    }
}