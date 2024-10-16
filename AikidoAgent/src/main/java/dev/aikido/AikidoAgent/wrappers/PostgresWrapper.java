package dev.aikido.AikidoAgent.wrappers;

import dev.aikido.AikidoAgent.Agent;
import dev.aikido.AikidoAgent.collectors.SQLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class PostgresWrapper extends Wrapper {

    public static AsmVisitorWrapper get() {
        // Wrap NativeQuery constructor.
        // https://github.com/pgjdbc/pgjdbc/blob/fcc13e70e6b6bb64b848df4b4ba6b3566b5e95a3/pgjdbc/src/main/java/org/postgresql/core/NativeQuery.java#L34C10-L34C21
        return Advice.to(PostgresAdvice.class)
                .on(ElementMatchers.isConstructor());
    }

    private static class PostgresAdvice {
        @Advice.OnMethodEnter
        public static void intercept(@Advice.Argument(0) String sql) {
            SQLCollector.report(sql, "postgresql", "postgresql.core.NativeQuery"); // Fix op
        }
    }
}
