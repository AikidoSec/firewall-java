package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class PostgresWrapper implements Wrapper {
    public String getName() {
        return JDBCConnectionAdvice.class.getName();
    }

    public ElementMatcher<? super MethodDescription> getMatcher() {
        return JDBCConnectionAdvice.getMatcher("org.postgresql.jdbc.PgConnection");
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.nameContains("org.postgresql.jdbc.PgConnection");
    }
}
