package dev.aikido.agent.wrappers.file;
import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_bootstrap.AikidoBootstrapClass;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * File(String parent, String child)
 */
public class FileConstructorMultiArgumentWrapper implements Wrapper {
    public String getName() {
        // Wrap File constructor.
        // https://docs.oracle.com/javase/8/docs/api/java/io/File.html
        return FileConstructorMultiArgumentAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(isSubTypeOf(File.class)).and(isConstructor()).and(
                takesArgument(0, String.class).and(takesArgument(1, String.class))
        );
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.isSubTypeOf(File.class);
    }

    public static class FileConstructorMultiArgumentAdvice {
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) String parent,
                @Advice.Argument(1) String child
        ) throws Throwable {
            try {
                String prop = System.getProperty("AIK_INTERNAL_coverage_run");
                if (prop != null && prop.equals("1")) {
                    return;
                }
            } catch (Throwable e) {
                return;
            }

            String operation = "java.io.File(String, String)";
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.FILE_COLLECTOR_REPORT, parent, operation);
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.FILE_COLLECTOR_REPORT, child, operation);
        }
    }
}
