package dev.aikido.agent.wrappers;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

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
        return ElementMatchers.isDeclaredBy(ElementMatchers.isSubTypeOf(Path.class))
                .and(named("resolve").or(named("resolveSibling").or(named("relativize"))));
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
                @Advice.Origin Executable method, @Advice.Argument(value = 0, optional = true) Object argument)
                throws Throwable {
            String jarFilePath = System.getProperty("AIK_agent_api_jar");
            URLClassLoader classLoader = null;
            try {
                URL[] urls = {new URL(jarFilePath)};
                classLoader = new URLClassLoader(urls);
            } catch (MalformedURLException ignored) {
            }
            if (classLoader == null) {
                return;
            }

            try {
                // Load the class from the JAR
                Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.collectors.FileCollector");

                // Run report with "argument"
                Method reportMethod = clazz.getMethod("report", Object.class, String.class);
                String op = "java.nio.file.Path." + method.getName();
                reportMethod.invoke(null, argument, op);
            } catch (InvocationTargetException invocationTargetException) {
                if (invocationTargetException
                        .getCause()
                        .toString()
                        .startsWith("dev.aikido.agent_api.vulnerabilities")) {
                    throw invocationTargetException.getCause();
                }
                // Ignore non-aikido throwables.
            } catch (Throwable e) {
            }
            classLoader.close(); // Close the class loader
        }
    }
}
