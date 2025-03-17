package dev.aikido.SpringWebfluxSampleApp;

import dev.aikido.agent_api.ShouldBlockRequest;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(2)
public class RatelimitingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             WebFilterChain webFilterChain) {
        ShouldBlockRequest.ShouldBlockRequestResult shouldBlockRequestResult = ShouldBlockRequest.shouldBlockRequest();
        if (shouldBlockRequestResult.block()) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            byte[] response = null;
            if (shouldBlockRequestResult.data().type().equals("ratelimited")) {
                String message = "You are rate limited by Zen.";
                if (shouldBlockRequestResult.data().trigger().equals("ip")) {
                    message = message + " (Your IP: " + shouldBlockRequestResult.data().ip() + ")";
                }
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS); // 429
                response = message.getBytes(StandardCharsets.UTF_8);
            } else if (shouldBlockRequestResult.data().type().equals("blocked")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN); // 403
                response = "You are blocked by Zen.".getBytes(StandardCharsets.UTF_8);
            }
            DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
            DataBuffer dataBuffer = dataBufferFactory.wrap(response);
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        }
        return webFilterChain.filter(exchange);
    }
}
