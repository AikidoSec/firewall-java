package dev.aikido.agent.wrappers;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class RuntimeExecWrapper implements Wrapper {
    @Override
    public String getName() {
        // Wrap Runtime.exec
        return RuntimeExecAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.any();
    }

    private static class RuntimeExecAdvice {
        @Advice.OnMethodEnter
        public static void intercept() {
            // NOP
        }
    }
}
