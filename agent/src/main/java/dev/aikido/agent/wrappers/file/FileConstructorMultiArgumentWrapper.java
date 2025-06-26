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
import java.net.URL;
import java.net.URLClassLoader;

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
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
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

                // Report both parent and child paths :
                reportMethod.invoke(null, parent, "java.io.File(String, String)");
                reportMethod.invoke(null, child, "java.io.File(String, String)");

                classLoader.close(); // Close the class loader
            } catch (InvocationTargetException invocationTargetException) {
                if (invocationTargetException.getCause().toString().startsWith("dev.aikido.agent_api.vulnerabilities")) {
                    throw invocationTargetException.getCause();
                }
                // Ignore non-aikido throwables.
                System.out.println("AIKIDO: " + invocationTargetException.getTargetException().getMessage());
            } catch (Throwable e) {
                System.out.println("AIKIDO: " + e.getMessage());
            }
        }
    }
}
