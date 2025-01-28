package dev.aikido.SpringWebfluxSampleApp;

import dev.aikido.agent_api.SetUser;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(1)
public class SetUserFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange,
                             WebFilterChain webFilterChain) {
        if (serverWebExchange.getRequest().getHeaders().containsKey("user")) {
            List<String> idList = serverWebExchange.getRequest().getHeaders().get("user");
            SetUser.setUser(new SetUser.UserObject(idList.get(0), "John Doe"));
        }
        return webFilterChain.filter(serverWebExchange);
    }
}