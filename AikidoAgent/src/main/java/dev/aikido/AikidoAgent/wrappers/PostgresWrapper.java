package dev.aikido.AikidoAgent.wrappers;

import dev.aikido.AikidoAgent.collectors.SQLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class PostgresWrapper implements Wrapper {
    public String getName() {
        // Wrap NativeQuery constructor.
        // https://github.com/pgjdbc/pgjdbc/blob/fcc13e70e6b6bb64b848df4b4ba6b3566b5e95a3/pgjdbc/src/main/java/org/postgresql/core/NativeQuery.java#L34C10-L34C21
        return PostgresAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("org.postgresql.core.NativeQuery"))
                .and(ElementMatchers.isConstructor());
    }

    public static class PostgresAdvice {
        @Advice.OnMethodEnter
        public static void intercept(@Advice.Origin Executable method,
                                         @Advice.AllArguments(readOnly = false, typing = DYNAMIC) Object[] args) {
            try {
                if(!method.getName().startsWith("org.postgresql.core.NativeQuery")) {
                    return;
                }
                if(args == null || args.length == 0) {
                    return;
                }
                if(args[0] instanceof String sql) {
                    SQLCollector.report(sql, "postgres", "postgresql.core.NativeQuery");
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
