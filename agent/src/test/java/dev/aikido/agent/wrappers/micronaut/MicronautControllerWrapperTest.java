package dev.aikido.agent.wrappers.micronaut;

import dev.aikido.agent_api.context.ContextObject;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static dev.aikido.agent.wrappers.micronaut.MicronautControllerWrapper.MicronautAdvice.buildContextObject;
import static dev.aikido.agent.wrappers.micronaut.MicronautControllerWrapper.MicronautAdvice.isRouteMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MicronautControllerWrapperTest {

    @Get
    void aRouteMethod() {
    }

    @RequestFilter
    void aFilterMethod() {
    }

    @Test
    void isRouteMethod_trueForRouteAnnotation() throws Exception {
        Method method = MicronautControllerWrapperTest.class.getDeclaredMethod("aRouteMethod");
        assertTrue(isRouteMethod(method));
    }

    @Test
    void isRouteMethod_falseForRequestFilterAnnotation() throws Exception {
        Method method = MicronautControllerWrapperTest.class.getDeclaredMethod("aFilterMethod");
        assertFalse(isRouteMethod(method));
    }

    @Test
    void buildContextObject_extractsMethodUrlIpAndCookies() throws Exception {
        HttpRequest<?> req = mock(HttpRequest.class);
        when(req.getMethodName()).thenReturn("GET");
        when(req.getUri()).thenReturn(new URI("/pets"));
        when(req.getRemoteAddress()).thenReturn(new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 1234));

        HttpParameters params = mock(HttpParameters.class);
        when(params.asMap()).thenReturn(Collections.emptyMap());
        when(req.getParameters()).thenReturn(params);

        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.asMap()).thenReturn(Collections.emptyMap());
        when(req.getHeaders()).thenReturn(headers);

        Cookie sessionCookie = mock(Cookie.class);
        when(sessionCookie.getName()).thenReturn("session");
        when(sessionCookie.getValue()).thenReturn("abc123");
        Cookies cookies = mock(Cookies.class);
        Set<Cookie> cookieSet = Collections.singleton(sessionCookie);
        when(cookies.getAll()).thenReturn(cookieSet);
        when(req.getCookies()).thenReturn(cookies);

        ContextObject context = buildContextObject(req);

        assertEquals("GET", context.getMethod());
        assertEquals("/pets", context.getUrl());
        assertEquals("1.2.3.4", context.getRemoteAddress());
        assertEquals("abc123", context.getCookies().get("session").get(0));
    }
}
