package dev.aikido.agent.wrappers.file;
import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

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
            } catch (Throwable e) {
                return;
            }
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
                reportMethod.invoke(null, argument, "java.io.File");
                classLoader.close(); // Close the class loader
            } catch (InvocationTargetException invocationTargetException) {
                if (invocationTargetException.getCause().toString().startsWith("dev.aikido.agent_api.vulnerabilities")) {
                    throw invocationTargetException.getCause();
                }
                // Ignore non-aikido throwables.
            } catch (Throwable e) {
                System.out.println("AIKIDO: " + e.getMessage());
            }
        }
    }
}
