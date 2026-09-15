package dev.aikido.agent.wrappers.executor;

import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.concurrent.ThreadPoolExecutor;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class ThreadPoolExecutorWrapper implements Wrapper {
    @Override
    public String getName() {
        return ExecuteAdvice.class.getName();
    }

    @Override
    public ElementMatcher getMatcher() {
        return isMethod()
            .and(named("execute"))
            .and(takesArguments(Runnable.class));
    }

    @Override
    public ElementMatcher getTypeMatcher() {
        return isSubTypeOf(ThreadPoolExecutor.class);
    }

    public static class ExecuteAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
            @Advice.Argument(value = 0, readOnly = false) Runnable task
        ) {
            task = ExecutorContextPropagation.wrap(task);
        }
    }
}
