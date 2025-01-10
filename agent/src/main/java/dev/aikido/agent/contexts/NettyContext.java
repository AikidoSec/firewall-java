package dev.aikido.agent.contexts;

import dev.aikido.agent_api.context.ContextObject;
import reactor.netty.http.server.HttpServerRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class NettyContext extends ContextObject {
    public NettyContext(
            String method, String uri, InetSocketAddress rawIp,
            HashMap<String, List<String>> cookies,
            Map<String, List<String>> query,
            List<Map.Entry<String, String>> headerEntries

    ) {
        this.method  = method;
        this.url = uri;
        this.cookies = cookies;
        this.query = new HashMap<>(query);
        this.headers = extractHeaders(headerEntries);

        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp.getAddress().getHostAddress(), this.headers);
        this.source = "ReactorNetty";
        this.redirectStartNodes = new ArrayList<>();

        // We don't have access yet to the route parameters.
        this.params = null;
    }

    private static HashMap<String, String> extractHeaders(List<Map.Entry<String, String>> entries) {
        HashMap<String, String> headers = new HashMap<>();
        for(Map.Entry<String, String> entry: entries) {
            headers.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return headers;
    };
}
