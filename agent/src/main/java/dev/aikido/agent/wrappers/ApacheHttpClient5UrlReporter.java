package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.RedirectCollector;
import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;

public final class ApacheHttpClient5UrlReporter {
    private static final String LAST_REQUEST_URI = "dev.aikido.apache-httpclient5.last-request-uri";

    private ApacheHttpClient5UrlReporter() {}

    public static void report(HttpRequest request, HttpRoute route, HttpClientContext context) {
        try {
            URI requestUri = request.getUri();
            URI currentUri = withTarget(requestUri, route.getTargetHost(), request.getScheme());
            URI previousUri = (URI) context.getAttribute(LAST_REQUEST_URI);
            RedirectLocations redirectLocations = context.getRedirectLocations();
            context.setAttribute(LAST_REQUEST_URI, currentUri);

            if (previousUri != null
                    && !previousUri.equals(currentUri)
                    && redirectLocations != null
                    && redirectLocations.contains(requestUri)
                    && Context.get() != null) {
                RedirectCollector.report(previousUri.toURL(), currentUri.toURL());
                return;
            }
            URLCollector.report(currentUri.toURL());
        } catch (AikidoException exception) {
            throw exception;
        } catch (Throwable ignored) {
        }
    }

    private static URI withTarget(URI requestUri, HttpHost target, String requestScheme) throws URISyntaxException {
        String scheme = target.getSchemeName();
        if (scheme == null) {
            scheme = requestScheme != null ? requestScheme : "http";
        }
        return new URI(
                scheme,
                null,
                target.getHostName(),
                target.getPort(),
                requestUri.getPath(),
                requestUri.getQuery(),
                requestUri.getFragment());
    }
}
