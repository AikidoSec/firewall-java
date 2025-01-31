package dev.aikido.agent_api.context;

import java.net.InetSocketAddress;
import java.util.*;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class SpringWebfluxContextObject extends SpringContextObject {
    public SpringWebfluxContextObject(
            String method, String uri, InetSocketAddress rawIp,
            HashMap<String, List<String>> cookies,
            Map<String, List<String>> query,
            Map<String, String> headerEntries

    ) {
        this.method  = method;
        this.url = uri;
        this.cookies = cookies;
        this.query = new HashMap<>(query);
        this.headers = extractHeaders(headerEntries);

        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp.getAddress().getHostAddress(), this.headers);
        this.source = "SpringWebflux";
        this.redirectStartNodes = new ArrayList<>();
    }

    private static HashMap<String, String> extractHeaders(Map<String, String> map) {
        HashMap<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            newMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return newMap;
    }
}
