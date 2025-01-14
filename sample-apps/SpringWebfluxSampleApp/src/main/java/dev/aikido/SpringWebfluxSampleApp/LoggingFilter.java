package dev.aikido.SpringWebfluxSampleApp;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Log the request path and method
        String path = exchange.getRequest().getURI().getPath();
        System.out.println("Request: " + exchange.getRequest().getMethod().toString() + " " + path);

        // Continue the filter chain
        return chain.filter(exchange).doOnSuccess(aVoid -> {
            // Optionally log the response status
            int statusCode = exchange.getResponse().getStatusCode() != null ?
                    exchange.getResponse().getStatusCode().value() : 0;
            System.out.println("Response Status: " + statusCode);
        });
    }
}
