package dev.aikido.agent.wrappers.spring;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.RedirectCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

public class SpringWebClientRedirectWrapper implements Wrapper {
    // Package-private in Reactor Netty, referenced by name only. This is the internal method
    // that runs once per redirect hop, for both WebClient and the Netty-backed RestClient -
    // Spring's own request-adaptation layer (ExchangeFunction/ReactorClientHttpRequest) is
    // only invoked once per top-level call and never sees redirect targets for bodiless (e.g.
    // GET) requests, since Reactor Netty resends internally without going back through it.
    // Mirrors HttpConnectionRedirectWrapper, which hooks the JDK's equally-internal
    // followRedirect0 for the same reason.
    private static final String HTTP_CLIENT_HANDLER_CLASS_NAME =
            "reactor.netty.http.client.HttpClientConnect$HttpClientHandler";

    public String getName() {
        return RedirectAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(getTypeMatcher())
                .and(ElementMatchers.named("redirect"))
                .and(ElementMatchers.takesArguments(1));
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.named(HTTP_CLIENT_HANDLER_CLASS_NAME);
    }
    public static class RedirectAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void after(@Advice.This Object handler) throws Exception {
            // fromURI/toURI are UriEndpoint (also package-private), both reassigned by
            // redirect() before this advice runs: fromURI is the hostname that redirected,
            // toURI is where it redirected to.
            String origin = externalForm(handler, "fromURI");
            String dest = externalForm(handler, "toURI");
            if (origin == null || dest == null) {
                return;
            }
            RedirectCollector.report(new URL(origin), new URL(dest));
        }

        // Must be public: after weaving, this is called as a real cross-class invocation from
        // inside the target class's own bytecode, so a private method would raise IllegalAccessError.
        public static String externalForm(Object handler, String fieldName) throws Exception {
            Field field = handler.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object uriEndpoint = field.get(handler);
            if (uriEndpoint == null) {
                return null;
            }
            Method toExternalForm = uriEndpoint.getClass().getDeclaredMethod("toExternalForm");
            toExternalForm.setAccessible(true);
            return (String) toExternalForm.invoke(uriEndpoint);
        }
    }
}
