package dev.aikido.agent.wrappers;

import dev.aikido.agent_bootstrap.AikidoBootstrapClass;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.URL;
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
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
                @Advice.Argument(0) HttpRequest httpRequest
        ) throws Throwable {
            if (httpRequest == null || httpRequest.uri() == null) {
                return;
            }
            URL url = httpRequest.uri().toURL();
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.URL_COLLECTOR_REPORT, url);
        }
    }
}
