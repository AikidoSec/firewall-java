package dev.aikido.agent.wrappers.executor;

import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class ForkJoinPoolWrapper implements Wrapper {
    @Override
    public String getName() {
        return ForkJoinAdvice.class.getName();
    }

    @Override
    public ElementMatcher getMatcher() {
        return isMethod()
            .and(named("execute").or(named("submit")))
            .and(
                takesArguments(Runnable.class)
                    .or(takesArguments(Callable.class))
                    .or(takesArguments(Runnable.class, Object.class))
            );
    }

    @Override
    public ElementMatcher getTypeMatcher() {
        return isSubTypeOf(ForkJoinPool.class);
    }

    public static class ForkJoinAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
            @Advice.Argument(value = 0, readOnly = false, typing = DYNAMIC) Object task
        ) {
            if (task instanceof Runnable) {
                task = ExecutorContextPropagation.wrap((Runnable) task);
            } else if (task instanceof Callable) {
                task = ExecutorContextPropagation.wrap((Callable) task);
            }
        }
    }
}
