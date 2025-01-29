package dev.aikido.agent.wrappers;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;


public class HttpClientSendWrapper implements Wrapper {
    public String getName() {
        // Wrap send(HttpRequest req, ...) and sendAsync(HttpRequest req, ...) on HttpClient instance
        // https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html#send(java.net.http.HttpRequest,java.net.http.HttpResponse.BodyHandler)
        // https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html#sendAsync(java.net.http.HttpRequest,java.net.http.HttpResponse.BodyHandler)
        return SendFunctionAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.isSubTypeOf(HttpClient.class))
                .and(ElementMatchers.named("send").or(ElementMatchers.named("sendAsync")));
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.isSubTypeOf(HttpClient.class);
    }
    public static class SendFunctionAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
                @Advice.Argument(0) HttpRequest httpRequest
        ) throws Exception {
            if (httpRequest == null || httpRequest.uri() == null) {
                return;
            }
            String jarFilePath = System.getProperty("AIK_agent_api_jar");
            URLClassLoader classLoader = null;
            try {
                URL[] urls = { new URL(jarFilePath) };
                classLoader = new URLClassLoader(urls);
            } catch (MalformedURLException ignored) {}
            if (classLoader == null) {
                return;
            }

            // Load the class from the JAR
            Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.collectors.URLCollector");

            // Run report with "argument"
            for (Method method2: clazz.getMethods()) {
                if(method2.getName().equals("report")) {
                    method2.invoke(null, httpRequest.uri().toURL());
                    break;
                }
            }
            classLoader.close(); // Close the class loader
        }
    }
}
