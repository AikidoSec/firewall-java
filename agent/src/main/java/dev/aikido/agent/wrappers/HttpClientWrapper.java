package dev.aikido.agent.wrappers;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.net.http.HttpRequest;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class HttpClientWrapper implements Wrapper {
    private static final Logger log = LogManager.getLogger(HttpClientWrapper.class);

    public String getName() {
        // Wrap getResponseCode function which executes HTTP requests
        // https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html#getResponseCode--
        return HttpClientAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("jdk.internal.net.http.HttpRequestImpl"))
                .and(ElementMatchers.nameContainsIgnoreCase("newInstanceForRedirection"));
    }
    public static class HttpClientAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file, specified with the AIKIDO_DIRECTORY env variable
        @Advice.OnMethodEnter
        public static void before(
                @Advice.This(typing = DYNAMIC, optional = true) Object target,
                @Advice.Argument(0) Object uriObject,
                @Advice.Argument(2) Object httpRequestObject
        ) {
            try {
                URI uri = (URI) uriObject;
                HttpRequest httpRequest = (HttpRequest) httpRequestObject;

                // Call to collector :
                String jarFilePath = System.getProperty("AIK_agent_api_jar");
                URL[] urls = { new URL(jarFilePath) };
                URLClassLoader classLoader = new URLClassLoader(urls);

                // Load the class from the JAR
                Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.collectors.RedirectCollector");

                // Run report func with its arguments
                for (Method method2: clazz.getMethods()) {
                    if(method2.getName().equals("report")) {
                        URL originUrl = httpRequest.uri().toURL();
                        method2.invoke(null, originUrl, uri.toURL());
                        break;
                    }
                }
                classLoader.close(); // Close the class loader
            } catch (Throwable ignored) {}
        }
    }
}
