package dev.aikido.agent.wrappers.spring;

import dev.aikido.agent.wrappers.Wrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

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
        // Registration happens in onExit, wrapped around the returned Mono via
        // deferContextual(), rather than eagerly in onEnter. That way it runs at subscribe
        // time, reading back whatever ContextObject SpringWebfluxWrapper wrote into Reactor's
        // Context (see ReactorAikidoContext) - reliable regardless of scheduler hops between
        // the incoming request and this WebClient call, unlike Context.get()'s ThreadLocal.
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void after(
                @Advice.Argument(0) ClientRequest request,
                @Advice.Return(readOnly = false) Mono<ClientResponse> returnValue
        ) throws MalformedURLException {
            if (request == null || request.url() == null || returnValue == null) {
                return;
            }
            URL url = request.url().toURL();
            returnValue = (Mono<ClientResponse>) ReactorAikidoContext.deferRegisterUrl(returnValue, url);
        }
    }
}
