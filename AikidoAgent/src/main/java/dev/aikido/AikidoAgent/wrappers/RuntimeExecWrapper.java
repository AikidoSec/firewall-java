package dev.aikido.AikidoAgent.wrappers;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.matcher.ElementMatchers;

public class RuntimeExecWrapper extends Wrapper {
    public static AsmVisitorWrapper get() {
        // Wrap Runtime.exec
        return Advice.to(RuntimeExecAdvice.class)
            .on(ElementMatchers.named("exec"));
    }

    private static class RuntimeExecAdvice {
        @Advice.OnMethodEnter
        public static void intercept() {
            // NOP
        }
    }
}
