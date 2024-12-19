package dev.aikido.agent.wrappers;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.net.*;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class URLConnectionWrapper implements Wrapper {
    public String getName() {
        // Wrap Constructor of URLConnection
        // https://docs.oracle.com/javase/8/docs/api/java/net/URLConnection.html
        return ConstructorAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isConstructor().and(isDeclaredBy(isSubTypeOf(HttpURLConnection.class).or(nameContainsIgnoreCase("URLConnection"))));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.isSubTypeOf(URLConnection.class).and(not(
                // Names to be ignored :
                nameContains("JarURLConnection")
                .or(nameContains("FileURLConnection"))
                .or(nameContains("JavaRuntimeURLConnection"))
                // Spring boot names to  be ignored :
                .or(nameContains("JarUrlConnection"))
                .or(nameContains("NestedUrlConnection"))
        ));
    }

    public static class ConstructorAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void before(
                @Advice.This(typing = DYNAMIC) Object target
        ) throws Exception {
            String jarFilePath = System.getProperty("AIK_agent_api_jar");
            URLClassLoader classLoader = null;
            try {
                URL[] urls = { new URL(jarFilePath) };
                classLoader = new URLClassLoader(urls);
            } catch (MalformedURLException ignored) {}
            if (classLoader == null) {
                return;
            }
            URL url = ((HttpURLConnection) target).getURL();
            if (target == null || url == null) {
                return;
            }
            // Load the class from the JAR
            Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.collectors.URLCollector");

            // Run report with "argument"
            for (Method method2: clazz.getMethods()) {
                if(method2.getName().equals("report")) {
                    method2.invoke(null, url);
                    break;
                }
            }
            classLoader.close(); // Close the class loader
        }
    }
}
