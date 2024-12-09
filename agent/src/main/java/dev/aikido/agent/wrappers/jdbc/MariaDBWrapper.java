package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class MariaDBWrapper implements Wrapper {
    public String getName() {
        return JDBCConnectionAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return JDBCConnectionAdvice.getMatcher("org.mariadb.jdbc");
    }
}
