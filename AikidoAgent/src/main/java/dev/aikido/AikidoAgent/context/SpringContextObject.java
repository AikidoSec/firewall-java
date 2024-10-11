package dev.aikido.AikidoAgent.context;

import java.util.HashMap;

public class SpringContextObject implements ContextObject{
    public void ContextObject() {
        // Initialize context object : Extract headers, cookies, etc.
    }
    @Override
    public HashMap<String, String> getHeaders() {
        return null;
    }
}
