package dev.aikido.agent.wrappers;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class InetAddressWrapper implements Wrapper {
    public String getName() {
        // Wrap getAllByName function which resolves hostnames to IP addresses
        // https://docs.oracle.com/javase/8/docs/api/java/net/InetAddress.html#getAllByName-java.lang.String-
        return InetAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.named("getAllByName");
    }
    public static class InetAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
        @Advice.OnMethodExit
        public static void after(
                @Advice.Enter String hostname,
                @Advice.Return InetAddress[] inetAddresses
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
                Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.collectors.HostnameCollector");

                // Run report with "argument"
                for (Method method2: clazz.getMethods()) {
                    if(method2.getName().equals("report")) {
                        method2.invoke(null, hostname, inetAddresses);
                        break;
                    }
                }
                classLoader.close(); // Close the class loader
            } catch (InvocationTargetException invocationTargetException) {
                if(invocationTargetException.getCause().toString().startsWith("dev.aikido.agent_api.vulnerabilities")) {
                    throw invocationTargetException.getCause();
                }
                // Ignore non-aikido throwables.
            } catch(Throwable e) {}
        }
        @Advice.OnMethodEnter
        public static String before(
                @Advice.This(typing = DYNAMIC, optional = true) Object target,
                @Advice.Origin Executable method,
                @Advice.Argument(0) Object argument
        ) {
            return argument.toString();
        }
    }
}
