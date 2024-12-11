package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.SQLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
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
