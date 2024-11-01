package dev.aikido.agent_api.context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

public class ContextObject {
    String method;
    String source;
    String url;
    String route;
    String remoteAddress;
    HashMap<String, String> headers;
    HashMap<String, String[]> query;
    HashMap<String, String> cookies;
    Object body;

    public Object getBody() {
        return body;
    }
    public void setBody(Object newBody) {
        body = newBody;
    }
    public String getMethod() {
        return method;
    }
    public String getSource() {
        return source;
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

    public HashMap<String, String[]> getQuery() {
        return query;
    }

    public HashMap<String, String> getCookies() {
        return cookies;
    }
    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public String getJSONBody() {
        Gson gson = new Gson();
        return gson.toJson(this.body);
    }

    public RouteMetadata getRouteMetadata() {
        return new RouteMetadata(route, url, method);
    }
}
