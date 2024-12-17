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
        // Body :
        if (!contextObject.getCache().containsKey("body")) {
            bodyStrings = StringExtractor.extractStringsFromObject(contextObject.getBody());
            contextObject.getCache().put("body", bodyStrings);
        } else {
            bodyStrings = contextObject.getCache().get("body");
        }
        // Query :
        if (!contextObject.getCache().containsKey("query")) {
            queryStrings = StringExtractor.extractStringsFromObject(contextObject.getQuery());
            contextObject.getCache().put("query", queryStrings);
        } else {
            queryStrings = contextObject.getCache().get("query");
        }
        // Headers :
        if (!contextObject.getCache().containsKey("headers")) {
            headersStrings = StringExtractor.extractStringsFromObject(contextObject.getHeaders());
            contextObject.getCache().put("headers", headersStrings);
        } else {
            headersStrings = contextObject.getCache().get("headers");
        }
        // Cookies :
        if (!contextObject.getCache().containsKey("cookies")) {
            cookieStrings = StringExtractor.extractStringsFromObject(contextObject.getCookies());
            contextObject.getCache().put("cookies", cookieStrings);
        } else {
            cookieStrings = contextObject.getCache().get("cookies");
        }
        // route parameters :
        if (!contextObject.getCache().containsKey("routeParams")) {
            routeParamStrings = StringExtractor.extractStringsFromObject(contextObject.getParams());
            contextObject.getCache().put("routeParams", routeParamStrings);
        } else {
            routeParamStrings = contextObject.getCache().get("routeParams");
        }
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
