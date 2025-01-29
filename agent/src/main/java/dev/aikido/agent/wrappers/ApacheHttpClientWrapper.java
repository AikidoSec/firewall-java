package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.URLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.net.URI;

public class ApacheHttpClientWrapper implements Wrapper {
    public String getName() {
        // Wrap newCall function which makes an HTTP Request
        // https://hc.apache.org/httpcomponents-client-5.4.x/current/httpclient5/apidocs/org/apache/hc/client5/http/classic/HttpClient.html#execute-org.apache.hc.core5.http.ClassicHttpRequest-
        return ApacheHttpClientAdvice.class.getName();
    }

    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("org.apache.http").and(ElementMatchers.nameContains("HttpClient")))
            .and(ElementMatchers.nameContainsIgnoreCase("execute"));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.nameContains("org.apache").and(ElementMatchers.nameEndsWith("HttpClient"));
    }

    public class ApacheHttpClientAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
            @Advice.Argument(0) Object request
        ) throws Throwable {
            if (request == null) {
                return;
            }
            // Fetch URI :
            URI uri = null;
            if (request.getClass().toString().contains("HttpRequest")) {
                // This Object is an HttpRequest object, we will use reflection to access the URL:
                // We want to (safely) access request.getUri()
                Method toUriMethod = request.getClass().getMethod("getUri");
                uri = (URI) toUriMethod.invoke(request);

            } else if (request.getClass().toString().contains("HttpHost")) {
                // This Object is an HttpHost object, we will use reflection to access the URL:
                // We want to (safely) access request.toURI() (which returns a string)
                Method toUriMethodString = request.getClass().getMethod("toURI");
                String uriString = (String) toUriMethodString.invoke(request);
                uri = new URI(uriString);
            }
            if (uri != null) {
                // Report the URL :
                URLCollector.report(uri.toURL());
            }
        }
    }
}
