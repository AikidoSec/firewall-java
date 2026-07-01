package dev.aikido.agent.wrappers.spring;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.URLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.net.MalformedURLException;

public class SpringWebClientWrapper implements Wrapper {
    // Referenced by name (not by .class) in the matchers below: ExchangeFunction is only on
    // the target application's classloader (spring-webflux is compileOnly here), not on the
    // agent's own classloader, so a .class literal would throw NoClassDefFoundError at premain.
    private static final String EXCHANGE_FUNCTION_CLASS_NAME =
            "org.springframework.web.reactive.function.client.ExchangeFunction";

    public String getName() {
        // Wrap exchange(ClientRequest) on ExchangeFunction, the interface every WebClient
        // request goes through before Reactor Netty resolves/connects.
        // https://docs.spring.io/spring-framework/docs/5.3.20/javadoc-api/org/springframework/web/reactive/function/client/ExchangeFunction.html
        return SpringWebClientAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(getTypeMatcher())
                .and(ElementMatchers.named("exchange"));
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers.named(EXCHANGE_FUNCTION_CLASS_NAME));
    }
    public static class SpringWebClientAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
                @Advice.Argument(0) ClientRequest request
        ) throws MalformedURLException {
            if (request == null || request.url() == null) {
                return;
            }
            // Report the URL before the request is sent, so DNSRecordCollector can match the
            // DNS lookup that follows to this outgoing request.
            URLCollector.report(request.url().toURL());
        }
    }
}
