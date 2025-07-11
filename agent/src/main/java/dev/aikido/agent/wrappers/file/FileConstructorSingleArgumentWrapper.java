package dev.aikido.agent.wrappers.file;
import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_bootstrap.AikidoBootstrapClass;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.net.URI;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * File(URI uri)
 * File(String pathname)
 */
public class FileConstructorSingleArgumentWrapper implements Wrapper {
    public String getName() {
        // Wrap File constructor.
        // https://docs.oracle.com/javase/8/docs/api/java/io/File.html
        return FileConstructorSingleArgumentAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(isSubTypeOf(File.class)).and(isConstructor()).and(
                takesArgument(0, String.class).or(takesArgument(0, URI.class))
        );
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.isSubTypeOf(File.class);
    }

    public static class FileConstructorSingleArgumentAdvice {
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) Object argument
        ) throws Throwable {
            try {
                String prop = System.getProperty("AIK_INTERNAL_coverage_run");
                if (prop != null && prop.equals("1")) {
                    return;
                }
            } catch (Throwable e) {
                return;
            }

            String op = "java.io.File";
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.FILE_COLLECTOR_REPORT, argument, op);
        }
    }
}
