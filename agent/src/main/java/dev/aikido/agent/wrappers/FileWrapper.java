package dev.aikido.agent.wrappers;
import dev.aikido.agent_api.collectors.FileCollector;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class FileWrapper implements Wrapper {
    public String getName() {
        // Wrap File constructor.
        // https://docs.oracle.com/javase/8/docs/api/java/io/File.html
        return FileAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(isSubTypeOf(File.class)).and(isConstructor()).and(takesArgument(0, String.class));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.isSubTypeOf(File.class);
    }

    public static class FileAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) Object argument
        ) throws Throwable {
            try {
                String prop = System.getProperty("AIK_INTERNAL_coverage_run");
                if (prop != null && prop.equals("1")) {
                    return;
                }
                if (Thread.currentThread().getClass().toString()
                        .equals("class dev.aikido.agent_api.background.BackgroundProcess")) {
                    return; // Do not wrap File calls in background process.
                }
                FileCollector.report(argument, "java.io.File");

            } catch (AikidoException e) {
                throw e;
            } catch (Throwable ignored) {}
        }
    }
}
