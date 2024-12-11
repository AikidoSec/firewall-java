package dev.aikido.agent.wrappers.jdbc;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.SQLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

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
