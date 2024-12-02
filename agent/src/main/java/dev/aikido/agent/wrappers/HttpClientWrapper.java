package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.RedirectCollector;
import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class HttpClientWrapper implements Wrapper {
    private static final Logger log = LogManager.getLogger(HttpClientWrapper.class);

    public String getName() {
        // Wrap getResponseCode function which executes HTTP requests
        // https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html#getResponseCode--
        return HttpClientAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("jdk.internal.net.http.HttpRequestImpl"))
                .and(ElementMatchers.nameContainsIgnoreCase("newInstanceForRedirection"));
    }
    public static class HttpClientAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file, specified with the AIKIDO_DIRECTORY env variable
        @Advice.OnMethodEnter
        public static void before(
                @Advice.This(typing = DYNAMIC, optional = true) Object target,
                @Advice.Argument(0) Object uriObject,
                @Advice.Argument(2) Object httpRequestObject
        ) throws AikidoException {
            try {
                // Cast :
                URI uri = (URI) uriObject;
                HttpRequest httpRequest = (HttpRequest) httpRequestObject;
                URL originUrl = httpRequest.uri().toURL();
                RedirectCollector.report(originUrl, uri.toURL());
            } catch(Throwable e) {
                if(e instanceof AikidoException aikidoException) {
                    throw aikidoException; // Do throw an Aikido vulnerability
                }
                // Ignore non-aikido throwables.
            }
        }
    }
}
