package dev.aikido.agent.wrappers.jdbc;

import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;

import dev.aikido.agent.wrappers.Wrapper;
import java.sql.Connection;
import java.sql.Statement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class MysqlCJWrapper implements Wrapper {
    public String getName() {
        return JDBCConnectionAdvice.class.getName();
    }

    public ElementMatcher<? super MethodDescription> getMatcher() {
        return JDBCConnectionAdvice.getMatcher("com.mysql.cj.jdbc");
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("com.mysql.cj.jdbc")
                .and(isSubTypeOf(Connection.class).or(isSubTypeOf(Statement.class)));
    }
}
