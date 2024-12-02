package dev.aikido.agent.wrappers;
import dev.aikido.agent_api.collectors.HostnameCollector;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;
import java.net.InetAddress;

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
        ) throws AikidoException {
            try {
                HostnameCollector.report(hostname, inetAddresses);
            } catch(Throwable e) {
                if(e instanceof AikidoException) {
                    throw e; // Do throw an Aikido vulnerability
                }
                // Ignore non-aikido throwables.
            }
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
