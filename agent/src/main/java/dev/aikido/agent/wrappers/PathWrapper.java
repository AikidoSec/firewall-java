package dev.aikido.agent.wrappers;
import dev.aikido.agent_api.collectors.FileCollector;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
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
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Origin Executable method,
                @Advice.Argument(value = 0, optional = true) Object argument
        ) throws Throwable {
            try {
                String op = "java.nio.file.Path." + method.getName();

                FileCollector.report(argument, op);
            } catch (AikidoException e) {
                throw e;
            } catch(Throwable e) {}
        }
    }
}
