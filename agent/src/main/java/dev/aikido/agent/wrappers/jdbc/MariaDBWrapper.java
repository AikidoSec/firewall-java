package dev.aikido.agent.wrappers.jdbc;

import static net.bytebuddy.matcher.ElementMatchers.*;

import dev.aikido.agent.wrappers.Wrapper;
import java.sql.Connection;
import java.sql.Statement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class MariaDBWrapper implements Wrapper {
    public String getName() {
        return JDBCConnectionAdvice.class.getName();
    }

    public ElementMatcher<? super MethodDescription> getMatcher() {
        return JDBCConnectionAdvice.getMatcher("org.mariadb.jdbc");
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("org.mariadb.jdbc")
                .and(isSubTypeOf(Connection.class).or(isSubTypeOf(Statement.class)));
    }
}
