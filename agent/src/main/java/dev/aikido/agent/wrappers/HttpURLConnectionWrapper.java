package dev.aikido.agent.wrappers;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class HttpURLConnectionWrapper implements Wrapper {
    public String getName() {
        // Wrap Constructor of HttpURLConnection
        // https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html
        return ConstructorAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("java.net.HttpURLConnection"))
                .and(ElementMatchers.isConstructor());
    }
    public static class ConstructorAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file, specified with the AIKIDO_DIRECTORY env variable
        @Advice.OnMethodExit
        public static void after(
                @Advice.This(typing = DYNAMIC, optional = true) HttpURLConnection target
        ) {
            String pathToAikidoFolder = System.getenv("AIKIDO_DIRECTORY");
            String jarFilePath = "file:" + pathToAikidoFolder + "agent_api.jar";
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
                Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.collectors.URLCollector");

                // Run report with "argument"
                for (Method method2: clazz.getMethods()) {
                    if(method2.getName().equals("report")) {
                        method2.invoke(null, target.getURL());
                        break;
                    }
                }
                classLoader.close(); // Close the class loader
            } catch(Throwable ignored) {}
        }
    }
}
