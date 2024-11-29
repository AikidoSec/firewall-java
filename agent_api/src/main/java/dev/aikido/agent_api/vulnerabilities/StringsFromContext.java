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
        bodyStrings = StringExtractor.extractStringsFromObject(contextObject.getBody());
        queryStrings = StringExtractor.extractStringsFromObject(contextObject.getQuery());
        headersStrings = StringExtractor.extractStringsFromObject(contextObject.getHeaders());
        cookieStrings = StringExtractor.extractStringsFromObject(contextObject.getCookies());
        routeParamStrings = StringExtractor.extractStringsFromObject(contextObject.getParams());
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
