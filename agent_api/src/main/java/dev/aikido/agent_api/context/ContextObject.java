package dev.aikido.agent_api.context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.RedirectNode;

import java.util.*;

public class ContextObject {
    protected String method;
    protected String source;
    protected String url;
    protected String route;
    protected String remoteAddress;
    protected HashMap<String, List<String>> headers;
    protected HashMap<String, List<String>> query;
    protected HashMap<String, List<String>> cookies;
    protected Object params;
    protected Object body;
    // Auxiliary :
    protected User user;
    protected String rateLimitGroup;
    protected boolean executedMiddleware;
    protected transient ArrayList<RedirectNode> redirectStartNodes;
    protected transient Map<String, Map<String, String>> cache = new HashMap<>();
    protected transient Optional<Boolean> forcedProtectionOff = Optional.empty();

    // We store hostnames in the context object so we can match a given hostname (by DNS request)
    // with its port number (which we know by instrumenting the URLs that get requested).
    protected transient Hostnames hostnames = new Hostnames(1000); // max 1000 entries

    public boolean middlewareExecuted() {return executedMiddleware; }
    public void setExecutedMiddleware(boolean value) { executedMiddleware = value; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRateLimitGroup() {
        return rateLimitGroup;
    }
    public void setRateLimitGroup(String groupId) {
        this.rateLimitGroup = groupId;
    }

    public Object getBody() {
        return body;
    }
    public void setBody(Object body) {
        this.body = body;
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

    public Object getParams() { return params; }
    public String getUrl() {
        return url;
    }
    public String getRoute() {
        return route;
    }
    public String getRemoteAddress() {
        return remoteAddress;
    }
    public HashMap<String, List<String>> getHeaders() {
        return headers;
    }
    public String getHeader(String key) {
        return getHeader(this.headers, key);
    }
    public static String getHeader(HashMap<String, List<String>> headers, String key) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry: headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                List<String> headerValues = entry.getValue();
                if (headerValues != null && !headerValues.isEmpty()) {
                    return headerValues.get(headerValues.size() - 1); // Last header is default
                }
            }
        }
        return null;
    }
    public HashMap<String, List<String>> getQuery() {
        return query;
    }
    public HashMap<String, List<String>> getCookies() {
        return cookies;
    }
    public Map<String, Map<String, String>> getCache() { return cache; }
    public Hostnames getHostnames() { return hostnames; }

    public void setForcedProtectionOff(boolean forcedProtectionOff) {
        this.forcedProtectionOff = Optional.of(forcedProtectionOff);
    }
    public Optional<Boolean> getForcedProtectionOff() {
        return this.forcedProtectionOff;
    }

    public RouteMetadata getRouteMetadata() {
        return new RouteMetadata(route, url, method);
    }

    @Override
    public String toString() {
        return "ContextObject{" +
            "method='" + method + '\'' +
            ", url='" + url + '\'' +
            ", source='" + source + '\'' +
            '}';
    }
}
