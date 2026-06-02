package dev.aikido.agent.wrappers.executor;

import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class ScheduledThreadPoolExecutorWrapper implements Wrapper {
    @Override
    public String getName() {
        return ScheduleAdvice.class.getName();
    }

    @Override
    public ElementMatcher getMatcher() {
        return isMethod()
            .and(named("schedule"))
            .and(
                takesArguments(Runnable.class, long.class, TimeUnit.class)
                    .or(takesArguments(Callable.class, long.class, TimeUnit.class))
            );
    }

    @Override
    public ElementMatcher getTypeMatcher() {
        return isSubTypeOf(ScheduledThreadPoolExecutor.class);
    }

    public static class ScheduleAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
            @Advice.Argument(value = 0, readOnly = false, typing = DYNAMIC) Object task
        ) throws Exception {
            if (task == null) {
                return;
            }

            // This advice is applied to JDK classes loaded by the bootstrap classloader.
            // Load agent_api reflectively because bootstrap classes cannot directly reference agent classes.
            String jarFilePath = System.getProperty("AIK_agent_api_jar");
            if (jarFilePath == null || jarFilePath.isBlank()) {
                return;
            }

            URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(jarFilePath) });
            Class<?> contextPropagationClass = classLoader.loadClass(
                "dev.aikido.agent_api.context.ContextPropagation"
            );

            if (task instanceof Runnable) {
                Method wrapRunnable = contextPropagationClass.getMethod("wrap", Runnable.class);
                task = wrapRunnable.invoke(null, task);
            } else if (task instanceof Callable) {
                Method wrapCallable = contextPropagationClass.getMethod("wrap", Callable.class);
                task = wrapCallable.invoke(null, task);
            }
        }
    }
}
