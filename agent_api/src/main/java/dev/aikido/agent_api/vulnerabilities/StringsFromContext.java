package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.ContentDispositionFilename;
import dev.aikido.agent_api.helpers.extraction.StringExtractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.aikido.agent_api.context.ContextObject.getHeader;

public class StringsFromContext {
    private final Map<String, String> bodyStrings;
    private final Map<String, String> queryStrings;
    private final Map<String, String> headersStrings;
    private final Map<String, String> cookieStrings;
    private final Map<String, String> routeParamStrings;

    public StringsFromContext(ContextObject contextObject) {
        bodyStrings = loadFromCache(contextObject, "body", contextObject.getBody());
        queryStrings = loadFromCache(contextObject, "query", contextObject.getQuery());

        HashMap<String, List<String>> headers = new HashMap<>(contextObject.getHeaders());

        // parse special headers like Content-Disposition to already extract certain values
        Optional<String> filename = ContentDispositionFilename.extract(contextObject.getHeader("Content-Disposition"));
        filename.ifPresent(s -> headers.put("Content-Disposition[filename]", List.of(s)));

        headersStrings = loadFromCache(contextObject, "headers", headers);

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
