package dev.aikido.agent.wrappers;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.*;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class HttpURLConnectionWrapper implements Wrapper {
    public String getName() {
        // Wrap Constructor of HttpURLConnection
        // https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html
        return ConstructorAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isConstructor().and(isDeclaredBy(is(HttpURLConnection.class)));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.is(HttpURLConnection.class);
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

            // report(URL) is overloaded (also has a report(URL, ContextObject) variant), so it
            // must be looked up by exact signature - matching by name alone could pick either.
            clazz.getMethod("report", URL.class).invoke(null, url);
            classLoader.close(); // Close the class loader
        }
    }
}
