package dev.aikido.agent.wrappers;

import dev.aikido.agent_bootstrap.AikidoBootstrapClass;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class HttpClientWrapper implements Wrapper {
    public String getName() {
        // Wrap getResponseCode function which executes HTTP requests
        // https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html#getResponseCode--
        return HttpClientAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("jdk.internal.net.http.HttpRequestImpl"))
                .and(ElementMatchers.nameContainsIgnoreCase("newInstanceForRedirection"));
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.isSubTypeOf(HttpClient.class);
    }
    public static class HttpClientAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
                @Advice.Argument(0) Object uriObject,
                @Advice.Argument(2) Object httpRequestObject
        ) throws Throwable {
            URL origin = ((HttpRequest) httpRequestObject).uri().toURL();
            URL dest = ((URI) uriObject).toURL();
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.REDIRECT_COLLECTOR_REPORT, origin, dest);
        }
    }
}
