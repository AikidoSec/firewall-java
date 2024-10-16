package dev.aikido.AikidoAgent.vulnerabilities;

import dev.aikido.AikidoAgent.context.ContextObject;
import dev.aikido.AikidoAgent.helpers.extraction.StringExtractor;

import java.util.Map;

public class StringsFromContext {
    private final Map<String, String> bodyStrings;
    private final Map<String, String> queryStrings;
    private final Map<String, String> headersStrings;
    public StringsFromContext(ContextObject contextObject) {
        bodyStrings = StringExtractor.extractStringsFromObject(contextObject.getBody());
        queryStrings = StringExtractor.extractStringsFromObject(contextObject.getQuery());
        headersStrings = StringExtractor.extractStringsFromObject(contextObject.getHeaders());

    }
    public Map<String, Map<String, String>> getAll() {
        return Map.of(
                "body", bodyStrings,
                "query", queryStrings,
                "headers", headersStrings
        );
    }
}
