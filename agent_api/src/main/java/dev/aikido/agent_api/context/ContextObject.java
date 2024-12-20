package dev.aikido.agent_api.context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aikido.agent_api.storage.RedirectNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ContextObject implements Serializable {
    protected String method;
    protected String source;
    protected String url;
    protected String route;
    protected String remoteAddress;
    protected HashMap<String, String> headers;
    protected HashMap<String, String[]> query;
    protected HashMap<String, String> cookies;
    protected Serializable params;
    protected transient Object body;
    // Auxiliary :
    protected User user;
    protected boolean executedMiddleware;
    protected transient ArrayList<RedirectNode> redirectStartNodes;
    protected transient Map<String, Map<String, String>> cache = new HashMap<>();

    public boolean middlewareExecuted() {return executedMiddleware; }
    public void setExecutedMiddleware(boolean value) { executedMiddleware = value; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Object getBody() {
        return body;
    }
    public void setBody(Object newBody) {
        body = newBody;
        this.cache.remove("body"); // Reset cache
    }

    public String getMethod() {
        return method;
    }
    public String getSource() {
        return source;
    }
    public List<RedirectNode> getRedirectStartNodes() { return redirectStartNodes; }
    public void addRedirectNode(RedirectNode node) { this.redirectStartNodes.add(node); }

    public Serializable getParams() { return params; }
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
    public Map<String, Map<String, String>> getCache() { return cache; }

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
