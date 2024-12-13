package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.SpringContextObject;

public final class ParamsCollector {
    private ParamsCollector() {}
    public static void report(Object params) {
        if (params != null) {
            if (Context.get() instanceof SpringContextObject springContextObject) {
                // Set path variables in context object :
                springContextObject.setParams(params);
                Context.set(springContextObject);
            }
        }
    }
}
