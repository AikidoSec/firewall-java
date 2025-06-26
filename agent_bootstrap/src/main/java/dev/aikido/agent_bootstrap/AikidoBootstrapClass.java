package dev.aikido.agent_bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public final class AikidoBootstrapClass {
    // Define some constants for the collectors we use:
    public final static Method URL_COLLECTOR_REPORT =
        load("URLCollector", "report", URL.class);

    public final static Method FILE_COLLECTOR_REPORT =
        load("FileCollector", "report", Object.class, String.class);

    public final static Method REDIRECT_COLLECTOR_REPORT =
        load("RedirectCollector", "report", URL.class, URL.class);

    public final static Method DNS_RECORD_COLLECTOR_REPORT =
        load("DNSRecordCollector", "report", String.class, InetAddress[].class);

    public final static Method COMMAND_COLLECTOR_REPORT =
        load("CommandCollector", "report", Object.class);

    // Since we have to wrap a native Java Class stuff gets more complicated
    // The classpath is not the same anymore, and we can't import our modules directly.
    // To bypass this issue we load collectors from a .jar file
    public static Method load(String className, String expectedMethodName, Class<?>... parameterTypes) {
        try {
            Class<?> requestedClass = loadClass("dev.aikido.agent_api.collectors." + className);
            return requestedClass.getMethod(expectedMethodName, parameterTypes);
        } catch (Throwable e) {
            System.out.println("AIKIDO: " + e.getMessage());
        }
        return null;
    }

    public static Object invoke(Method method, Object... arguments) throws Throwable {
        try {
            // Do not wrap calls that happen in the background process.
            if (Thread.currentThread().getClass().toString()
                .equals("class dev.aikido.agent_api.background.BackgroundProcess")) {
                return null;
            }

            return method.invoke(null, arguments);
        } catch (InvocationTargetException invocationTargetException) {
            if (invocationTargetException.getCause().toString().startsWith("dev.aikido.agent_api.vulnerabilities")) {
                throw invocationTargetException.getCause();
            }
            // Ignore non-aikido exceptions
            System.out.println("AIKIDO: " + invocationTargetException.getTargetException().getMessage());
        } catch (Throwable e) {
            System.out.println("AIKIDO: " + e.getMessage());
        }
        return null;
    }

    /**
     * Load class path from the aikido agent api jar file
     */
    private static Class<?> loadClass(String classPath) throws MalformedURLException, ClassNotFoundException {
        String jarFilePath = System.getProperty("AIK_agent_api_jar");
        URL[] urls = {new URL(jarFilePath)};
        URLClassLoader classLoader = new URLClassLoader(urls);
        return classLoader.loadClass(classPath);
    }
}
