package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.SpringContextObject;

import java.io.Serializable;

public final class RequestBodyCollector {
    private RequestBodyCollector() {}
    /**
     * Handles body of request (which comes later than request)
     * @param body can be any object representing incoming data.
     */
    public static void report(Object body) {
        ContextObject contextObj = Context.get();
        contextObj.setBody(body);
        Context.set(contextObj);
    }

    /**
     * Handles body of request (which comes later than request)
     * @param body can be any object representing incoming data.
     */
    public static void report(String identifier, Object body) {
        SpringContextObject contextObj = (SpringContextObject) Context.get();
        contextObj.setBodyElement(identifier, body);
        Context.set(contextObj);
    }
}
