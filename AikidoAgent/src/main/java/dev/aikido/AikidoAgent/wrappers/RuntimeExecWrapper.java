package dev.aikido.AikidoAgent.wrappers;

import dev.aikido.AikidoAgent.collectors.SQLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Optional;

public class RuntimeExecWrapper {
    public static AsmVisitorWrapper get() {
        // Wrap Runtime.exec
        return Advice.to(RuntimeExecAdvice.class)
            .on(ElementMatchers.named("exec"));
    }

    private static class RuntimeExecAdvice {
        @Advice.OnMethodEnter
        public static void intercept() {
            System.out.println("INTERCEPT INTERCEPT INTERCEPT INTERCEPT");
            //SQLCollector.report(sql, "postgresql");
        }
    }
}
