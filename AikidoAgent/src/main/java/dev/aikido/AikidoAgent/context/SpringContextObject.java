package dev.aikido.AikidoAgent.context;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;

public class SpringContextObject extends ContextObject{
    private final String source = "SpringFramework";
    public SpringContextObject(HttpServletRequest request) {
        this.method = request.getMethod();
        this.url = request.getRequestURL().toString();
        this.remoteAddress = request.getRemoteAddr();
        this.headers = null;
    }
}
