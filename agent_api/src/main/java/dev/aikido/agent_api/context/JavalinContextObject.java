package dev.aikido.agent_api.context;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JavalinContextObject extends JakartaContextObject {
    protected Map<String, String> params;
    public JavalinContextObject(HttpServletRequest request) {
        super(request);
        this.source = "Javalin";
        this.redirectStartNodes = new ArrayList<>();

        // We don't yet have access to the route parameters, so they will be set later.
        this.params = new HashMap<>();
    }
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    public void setParam(String key, String value) {
        if(!this.params.containsKey(key)) {
            this.params.put(key, value);
        }
    }
    public Serializable getParams() {
        return (Serializable) params;
    }
}
