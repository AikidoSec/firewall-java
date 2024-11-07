package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;

public class WebRequestCollector {
    /**
     * This function gets called in the initial phases of a request.
     * @param newContext is the new ContextObject that holds headers, query, ...
     */
    public static void report(ContextObject newContext) {

        // Set new context :
        Context.reset();
        Context.set(newContext);
    }
}
