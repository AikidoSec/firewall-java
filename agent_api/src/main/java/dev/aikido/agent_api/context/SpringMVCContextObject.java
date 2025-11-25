package dev.aikido.agent_api.context;

import java.util.*;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class SpringMVCContextObject extends SpringContextObject {
    public SpringMVCContextObject(
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
        this.source = "SpringFramework";
        this.redirectStartNodes = new ArrayList<>();
    }

    private HashMap<String, List<String>> extractHeaders(HashMap<String, Enumeration<String>> headers) {
        HashMap<String, List<String>> extractedHeaders = new HashMap<>();

        for (Map.Entry<String, Enumeration<String>> entry : headers.entrySet()) {
            Enumeration<String> valuesEnum = entry.getValue();
            List<String> valuesList = new ArrayList<>();
            while (valuesEnum.hasMoreElements()) {
                valuesList.add(valuesEnum.nextElement()); // Add each value to the list
            }

            String key = entry.getKey().toLowerCase(); // Convert key to lowercase
            extractedHeaders.put(key, valuesList);
        }

        return extractedHeaders;
    }

    @Override
    public Object getBody() {
        if (this.body != null) {
            // @RequestBody was used, all data is available :
            return this.body;
        }
        return this.bodyMap; // Use the selected fields that were extracted.
    }

    private static HashMap<String, List<String>> extractQueryParameters(Map<String, String[]> parameterMap) {
        HashMap<String, List<String>> query = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            // Convert String[] to List<String>
            List<String> list = Arrays.asList(entry.getValue());
            query.put(entry.getKey(), list);
        }
        return query;
    }
}
