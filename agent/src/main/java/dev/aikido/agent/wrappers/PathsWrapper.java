package dev.aikido.agent.wrappers;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * This class wraps the static get() function of Paths, this function is used
 * to convert user input into a Path which leads to Path Traversal
 * - Paths.get(...)
 * See oracle docs for more: https://docs.oracle.com/javase/8/docs/api/java/nio/file/Paths.html#get-java.lang.String-java.lang.String...-
 */
public class PathsWrapper implements Wrapper {
    public String getName() {
        return GetFunctionAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(nameContains("java.nio.file.Paths")).and(named("get")).and(takesArgument(0, String.class));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return isSubTypeOf(Paths.class);
    }

    public static class GetFunctionAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) String argument1,
                @Advice.Argument(value = 1, optional = true) String[] argument2
            ) throws Throwable {
            String jarFilePath = System.getProperty("AIK_agent_api_jar");
            URLClassLoader classLoader = null;
            try {
                URL[] urls = { new URL(jarFilePath) };
                classLoader = new URLClassLoader(urls);
            } catch (MalformedURLException ignored) {}
            if (classLoader == null) {
                return;
            }

            try {
                // Load the class from the JAR
                Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.collectors.FileCollector");

                // Run report with "argument"
                Method reportMethod = clazz.getMethod("report", Object.class, String.class);
                
                // Compose the path as Java's Paths.get does
                String composedPath = composePathsGet(argument1, argument2);
                reportMethod.invoke(null, composedPath, "java.nio.file.Paths.get");
            } catch (InvocationTargetException invocationTargetException) {
                if(invocationTargetException.getCause().toString().startsWith("dev.aikido.agent_api.vulnerabilities")) {
                    throw invocationTargetException.getCause();
                }
                // Ignore non-aikido throwables.
            } catch(Throwable e) {
                System.out.println("AIKIDO: " + e.getMessage());
            }
            classLoader.close(); // Close the class loader
        }

        /**
         * Composes a path from first and more components, mimicking Java's Paths.get(String, String...) method.
         * This ensures the detector sees the actual path that will be created by Paths.get.
         * 
         * Based on Java's Path.of() / Paths.get() behavior:
         * - Joins path segments with the file separator
         * - If a segment is absolute (starts with separator or has drive letter), it replaces all previous segments
         * - Empty segments are skipped
         */
        private static String composePathsGet(String first, String[] more) {
            if (first == null) {
                first = "";
            }
            if (more == null || more.length == 0) {
                return first;
            }
            
            StringBuilder result = new StringBuilder(first);
            String separator = System.getProperty("file.separator", "/");
            boolean isWindows = separator.equals("\\");
            
            for (String segment : more) {
                if (segment == null || segment.isEmpty()) {
                    continue;
                }
                
                // Check if segment is absolute
                boolean isAbsolute = false;
                if (isWindows) {
                    // Windows: absolute if has drive letter or UNC path
                    isAbsolute = (segment.length() >= 2 && segment.charAt(1) == ':') ||
                                 segment.startsWith("\\\\");
                } else {
                    // Unix: absolute if starts with '/'
                    isAbsolute = segment.startsWith("/");
                }
                
                if (isAbsolute) {
                    // Absolute segment replaces everything before it
                    result = new StringBuilder(segment);
                } else {
                    // Append segment to result
                    if (result.length() > 0 && !result.toString().endsWith("/") && !result.toString().endsWith("\\")) {
                        result.append(separator);
                    }
                    result.append(segment);
                }
            }
            
            return result.toString();
        }
    }
}
