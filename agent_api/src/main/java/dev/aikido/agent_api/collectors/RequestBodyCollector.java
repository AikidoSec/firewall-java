package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;

import java.io.Serializable;

public final class RequestBodyCollector {
    private RequestBodyCollector() {}
    /**
     * Handles body of request (which comes later than request)
     * @param body can be any object representing incoming data.
     */
    public static void report(String identifier, Object body) {
        ContextObject contextObj = Context.get();
        contextObj.setBodyElement(identifier, body);
        Context.set(contextObj);
    }
}
