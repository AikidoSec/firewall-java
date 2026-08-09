package dev.aikido.agent_api.context;

import java.util.*;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class RingContextObject extends ContextObject {
    public RingContextObject(
            String method, StringBuffer url, String rawIp, Map<String, String[]> queryParams,
            HashMap<String, List<String>> cookies, HashMap<String, Enumeration<String>> headers, String queryString
    ) {
        this.method = method;
        if (url != null) {
            this.url = url.toString();
            if (queryString != null && !queryString.isEmpty()) {
                this.url = this.url + "?" + queryString;
            }
        }
        this.query = extractQueryParameters(queryParams);
        this.cookies = cookies;
        this.headers = extractHeaders(headers);
        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp, this.headers);
        this.source = "Ring";
        this.redirectStartNodes = new ArrayList<>();
    }

    private static HashMap<String, List<String>> extractHeaders(HashMap<String, Enumeration<String>> headers) {
        HashMap<String, List<String>> extractedHeaders = new HashMap<>();
        for (Map.Entry<String, Enumeration<String>> entry : headers.entrySet()) {
            List<String> values = new ArrayList<>();
            Enumeration<String> valuesEnum = entry.getValue();
            while (valuesEnum.hasMoreElements()) {
                values.add(valuesEnum.nextElement());
            }
            extractedHeaders.put(entry.getKey().toLowerCase(), values);
        }
        return extractedHeaders;
    }

    private static HashMap<String, List<String>> extractQueryParameters(Map<String, String[]> parameterMap) {
        HashMap<String, List<String>> query = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            query.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return query;
    }
}
