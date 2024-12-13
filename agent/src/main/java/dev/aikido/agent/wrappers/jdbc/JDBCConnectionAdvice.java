package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent_api.collectors.SQLCollector;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

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
                SQLCollector.report(sql, dialect, operation);

            } catch (AikidoException e) {
                throw e;
            } catch (Throwable e) {
                logger.debug(e);
            }
        }
    }
}