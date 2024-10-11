package dev.aikido.AikidoAgent.context;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ContextObject {
    String method;
    String url;
    String route;
    String remoteAddress;
    HashMap<String, String> headers;
    HashMap<String, ArrayList<String>> query;
    HashMap<String, String> cookies;

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getRoute() {
        return route;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public HashMap<String, ArrayList<String>> getQuery() {
        return query;
    }

    public HashMap<String, String> getCookies() {
        return cookies;
    }
}
