package dev.aikido.agent_api.context;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class SpringContextObject extends JakartaContextObject {
    public SpringContextObject(HttpServletRequest request) {
        super(request);
        this.source = "SpringFramework";
        this.redirectStartNodes = new ArrayList<>();

        // We don't have access yet to the route parameters: doFilter() is called before the Controller
        // So the parameters will be set later.
        this.params = null;
    }
    public void setParams(Serializable params) {
        this.params = params;
    }
}
