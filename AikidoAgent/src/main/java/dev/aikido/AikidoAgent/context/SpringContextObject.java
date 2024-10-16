package dev.aikido.AikidoAgent.context;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.HashMap;

import static dev.aikido.AikidoAgent.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class SpringContextObject extends ContextObject{
    public SpringContextObject(HttpServletRequest request) {
        this.method = request.getMethod();
        this.url = request.getRequestURL().toString();
        this.remoteAddress = request.getRemoteAddr();
        this.headers = extractHeaders(request);
        this.query = extractQueryParameters(request);
        this.cookies = extractCookies(request);
        this.route = buildRouteFromUrl(this.url);
        this.source = "SpringFramework";
    }
    private static HashMap<String, String> extractHeaders(HttpServletRequest request) {
        HashMap<String, String> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headersMap.put(headerName, headerValue);
        }

        return headersMap;
    }
    private static HashMap<String, String[]> extractQueryParameters(HttpServletRequest request) {
        return new HashMap<>(request.getParameterMap());
    }
    private static HashMap<String, String> extractCookies(HttpServletRequest request) {
        HashMap<String, String> cookiesMap = new HashMap<>();
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookiesMap.put(cookie.getName(), cookie.getValue());
            }
        }

        return cookiesMap;
    }
}
