package dev.aikido.agent.context;

import dev.aikido.agent_api.context.ContextObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class SpringContextObject extends ContextObject {
    // We use this map for when @RequestBody does not get used :
    protected transient Map<String, Object> bodyMap = new HashMap<>();
    public SpringContextObject(HttpServletRequest request) {
        this.method = request.getMethod();
        if (request.getRequestURL() != null) {
            this.url = request.getRequestURL().toString();
        }
        String rawIp = request.getRemoteAddr();
        this.headers = extractHeaders(request);
        this.query = extractQueryParameters(request);
        this.cookies = extractCookies(request);
        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp, this.headers);
        this.source = "SpringFramework";
        this.redirectStartNodes = new ArrayList<>();

        // We don't have access yet to the route parameters: doFilter() is called before the Controller
        // So the parameters will be set later.
        this.params = null;
    }
    @Override
    public Object getBody() {
        if (this.body != null) {
            // @RequestBody was used, all data is available :
            return this.body;
        }
        return this.bodyMap; // Use the selected fields that were extracted.
    }
    public void setBodyElement(String key, Object value) {
        bodyMap.put(key, value);
        cache.remove("body"); // Reset body cache.
    }
    public void setParams(Object params) {
        this.params = params;
        this.cache.remove("routeParams"); // Reset cache
    }
    private static HashMap<String, String> extractHeaders(HttpServletRequest request) {
        HashMap<String, String> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
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
