package dev.aikido.agent_api.context;

public class SanitizedContextObject extends ContextObject {
    public SanitizedContextObject(ContextObject prev) {
        // Copy over all important attributes:
        this.body = prev.getBody();
        this.cookies = prev.getCookies();
        this.query = prev.getQuery();
        this.headers = prev.getHeaders();
        this.params = prev.getParams();
        this.method = prev.getMethod();
        this.remoteAddress = prev.getRemoteAddress();
        this.source = prev.getSource();
        this.user = prev.getUser();
        this.route = prev.getRoute();
        this.executedMiddleware = prev.middlewareExecuted();
    }
}
