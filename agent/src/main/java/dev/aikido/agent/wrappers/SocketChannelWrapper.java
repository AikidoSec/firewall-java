package dev.aikido.agent.wrappers;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.SocketChannel;

public class SocketChannelWrapper implements Wrapper {
    public String getName() {
        // Wrap connect(SocketAddress) on SocketChannel. Clients that resolve hostnames with
        // their own DNS resolver instead of InetAddress.getAllByName() (e.g. Reactor Netty's
        // async resolver, used by default by Spring's WebClient) never trigger
        // InetAddressWrapper, so this is the only point where we see the resolved address
        // before the connection is made. Also catches literal IP targets, which never go
        // through any resolver at all.
        // https://docs.oracle.com/javase/8/docs/api/java/nio/channels/SocketChannel.html#connect-java.net.SocketAddress-
        return SocketChannelAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.named("connect");
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.isSubTypeOf(SocketChannel.class);
    }
    public static class SocketChannelAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) SocketAddress remoteAddress
        ) throws Throwable {
            if (!(remoteAddress instanceof InetSocketAddress)) {
                return;
            }
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
            InetAddress resolvedAddress = inetSocketAddress.getAddress();
            if (resolvedAddress == null) {
                // Unresolved: nothing to report yet, connect() will throw on its own.
                return;
            }
            String hostname = inetSocketAddress.getHostString();

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
                Class<?> clazz = classLoader.loadClass("dev.aikido.agent_api.collectors.DNSRecordCollector");

                // Run reportConnect with "argument"
                for (Method method2: clazz.getMethods()) {
                    if(method2.getName().equals("reportConnect")) {
                        method2.invoke(null, hostname, resolvedAddress);
                        break;
                    }
                }
                classLoader.close(); // Close the class loader
            } catch (InvocationTargetException invocationTargetException) {
                if(invocationTargetException.getCause().toString().startsWith("dev.aikido.agent_api.vulnerabilities")) {
                    throw invocationTargetException.getCause();
                }
                // Ignore non-aikido throwables.
            } catch(Throwable e) {
                System.out.println("AIKIDO: " + e.getMessage());
            }
        }
    }
}
