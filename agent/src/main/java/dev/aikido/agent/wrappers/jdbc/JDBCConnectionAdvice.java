package dev.aikido.agent.wrappers.jdbc;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static dev.aikido.agent.helpers.ClassLoader.fetchMethod;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public final class JDBCConnectionAdvice {
    public static final Logger logger = LogManager.getLogger(JDBCConnectionAdvice.class);
    private JDBCConnectionAdvice() {}
    public static ElementMatcher<? super MethodDescription> getMatcher(String module) {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase(module)).and(
                ElementMatchers.named("prepareStatement")
                        .or(ElementMatchers.named("prepareCall"))
                        .or(ElementMatchers.named("nativeSQL"))
        );
    }

    /* Wraps all functions accepting sql for the Connection interface :
     * -> prepareStatement(sql, [...]), prepareCall(sql, [...]), nativeSQL(sql)
     * https://docs.oracle.com/javase/8/docs/api/java/sql/Connection.html#nativeSQL-java.lang.String-
     */
    @Advice.OnMethodEnter
    public static void before(
            @Advice.This(typing = DYNAMIC, optional = true) Connection connection,
            @Advice.Origin Executable method,
            @Advice.Argument(0) String sql
    ) throws Throwable {
        if (sql != null) {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String operation = "(" + metaData.getDriverName() + ") java.sql.Connection." + method.getName();
                String dialect = metaData.getDatabaseProductName().toLowerCase();

                Method reportSqlMethod = fetchMethod("dev.aikido.agent_api.collectors.SQLCollector", "report");
                reportSqlMethod.invoke(null, sql, dialect, operation);
            } catch (InvocationTargetException invocationTargetException) {
                if(invocationTargetException.getCause().toString().startsWith("dev.aikido.agent_api.vulnerabilities")) {
                    throw invocationTargetException;
                }
            } catch (Throwable e) {
                logger.debug(e);
            }
        }
    }
}