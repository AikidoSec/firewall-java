package dev.aikido.agent_api.context;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.ArrayList;

public class JavalinContextObject extends JakartaContextObject {
    public JavalinContextObject(HttpServletRequest request) {
        super(request);
        this.source = "Javalin";
        this.redirectStartNodes = new ArrayList<>();

        // We don't yet have access to the route parameters, so they will be set later.
        this.params = null;
    }
    public void setParams(Serializable params) {
        this.params = params;
    }
}
