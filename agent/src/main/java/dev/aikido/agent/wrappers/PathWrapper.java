package dev.aikido.agent.wrappers;
import dev.aikido.agent_bootstrap.AikidoBootstrapClass;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;
import java.nio.file.Path;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * This class wraps functions on the Path class, so once a Path object already exists,
 * The following functions can still accept user input :
 * - resolve(String|Path other)
 * - relativize(Path other)
 * - resolveSibling(String|Path other)
 * See Oracle docs for more: https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html
 */
public class PathWrapper implements Wrapper {
    public String getName() {
        return PathAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.isSubTypeOf(Path.class)).and(
                named("resolve").or(named("resolveSibling").or(named("relativize"))));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return isSubTypeOf(Path.class).or(isDeclaredBy(Path.class));
    }

    public static class PathAdvice {
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Origin Executable method,
                @Advice.Argument(value = 0, optional = true) Object argument
        ) throws Throwable {
            String op = "java.nio.file.Path." + method.getName();
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.FILE_COLLECTOR_REPORT, argument, op);
        }
    }
}
