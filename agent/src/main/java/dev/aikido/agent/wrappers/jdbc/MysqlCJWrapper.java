package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.nameContains;

public class MysqlCJWrapper implements Wrapper {
    public String getName() {
        return JDBCConnectionAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return JDBCConnectionAdvice.getMatcher("com.mysql.cj.jdbc.ConnectionImpl");
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("com.mysql.cj.jdbc.ConnectionImpl");
    }
}
