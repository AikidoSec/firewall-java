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
            Map<String, List<String>> headers

    ) {
        this.method  = method;
        this.url = uri;
        this.cookies = cookies;
        this.query = new HashMap<>(query);
        this.headers = extractHeaders(headers);

        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp.getAddress().getHostAddress(), this.getHeader("x-forwarded-for"));
        this.source = "SpringWebflux";
        this.redirectStartNodes = new ArrayList<>();
    }

    private static HashMap<String, List<String>> extractHeaders(Map<String, List<String>> map) {
        HashMap<String, List<String>> newMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            newMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return newMap;
    }
}
