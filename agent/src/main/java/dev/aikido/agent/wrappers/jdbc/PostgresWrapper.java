package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.sql.Connection;
import java.sql.Statement;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class PostgresWrapper implements Wrapper {
    public String getName() {
        return JDBCConnectionAdvice.class.getName();
    }

    public ElementMatcher<? super MethodDescription> getMatcher() {
        return JDBCConnectionAdvice.getMatcher("org.postgresql.jdbc");
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("org.postgresql.jdbc")
                .and(isSubTypeOf(Connection.class).or(isSubTypeOf(Statement.class)));
    }
}
