package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.extraction.StringExtractor;

import java.util.Map;

public class StringsFromContext {
    private final Map<String, String> bodyStrings;
    private final Map<String, String> queryStrings;
    private final Map<String, String> headersStrings;
    private final Map<String, String> cookieStrings;
    private final Map<String, String> routeParamStrings;

    public StringsFromContext(ContextObject contextObject) {
        bodyStrings = loadFromCache(contextObject, "body", contextObject.getBody());
        queryStrings = loadFromCache(contextObject, "query", contextObject.getQuery());
        headersStrings = loadFromCache(contextObject, "headers", contextObject.getHeaders());
        cookieStrings = loadFromCache(contextObject, "cookies", contextObject.getCookies());
        routeParamStrings = loadFromCache(contextObject, "routeParams", contextObject.getParams());
    }
    private static Map<String, String> loadFromCache(ContextObject contextObject, String prop, Object data) {
        if (contextObject.getCache().containsKey(prop)) {
            return contextObject.getCache().get(prop);
        }

        Map<String, String> extractedStrings = StringExtractor.extractStringsFromObject(data);
        if (extractedStrings != null) {
            contextObject.getCache().put(prop, extractedStrings);
            return extractedStrings;
        }

        return Map.of();
    }
    public Map<String, Map<String, String>> getAll() {
        return Map.of(
                "body", bodyStrings,
                "query", queryStrings,
                "headers", headersStrings,
                "cookies", cookieStrings,
                "routeParams", routeParamStrings
        );
    }
}
