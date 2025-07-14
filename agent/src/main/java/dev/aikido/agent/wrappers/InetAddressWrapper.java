package dev.aikido.agent.wrappers;
import dev.aikido.agent_bootstrap.AikidoBootstrapClass;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.InetAddress;

public class InetAddressWrapper implements Wrapper {
    public String getName() {
        // Wrap getAllByName function which resolves hostnames to IP addresses
        // https://docs.oracle.com/javase/8/docs/api/java/net/InetAddress.html#getAllByName-java.lang.String-
        return InetAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.named("getAllByName");
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.isSubTypeOf(InetAddress.class);
    }
    public static class InetAdvice {
        @Advice.OnMethodExit
        public static void after(
                @Advice.Argument(0) String hostname,
                @Advice.Return InetAddress[] inetAddresses
        ) throws Throwable {
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.DNS_RECORD_COLLECTOR_REPORT, hostname, inetAddresses);
        }
    }
}
