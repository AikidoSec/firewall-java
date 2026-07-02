package dev.aikido.SpringWebfluxSampleApp;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

@RestController
@RequestMapping("/api/request")
public class RequestController {
    private record UrlRequest(String url) {}

    private static final WebClient webClient = WebClient.create();

    // A separate client with followRedirect enabled, to exercise SSRF detection across
    // redirects (see SpringWebClientRedirectWrapper).
    private static final WebClient webClientFollowingRedirects = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
            .build();

    @PostMapping
    public Mono<String> makeRequest(@RequestBody UrlRequest urlRequest) {
        return makeRequestInternal(urlRequest.url());
    }

    // Query params are a tracked taint source for Spring WebFlux (unlike the request body),
    // so this variant is used to exercise SSRF detection end to end.
    @GetMapping
    public Mono<String> makeRequestFromQuery(@RequestParam String url) {
        return makeRequestInternal(url);
    }

    @GetMapping("/follow-redirects")
    public Mono<String> makeRequestFollowingRedirects(@RequestParam String url) {
        return webClientFollowingRedirects.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> isAikidoBlock(e)
                        ? Mono.error(e)
                        : Mono.just("Error: " + e.getMessage()));
    }

    // Exercises a scheduler hop (a common pattern for mixing blocking JDBC with reactive
    // controllers) BEFORE the WebClient call, to test whether ThreadLocal-based taint/port
    // correlation survives moving off the original reactor-http-nio thread. Unverified
    // hypothesis, see PR #312 worklog item 2.
    @GetMapping("/publish-on")
    public Mono<String> makeRequestWithSchedulerHop(@RequestParam String url) {
        return Mono.just(url)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(this::makeRequestInternal);
    }

    private Mono<String> makeRequestInternal(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> isAikidoBlock(e)
                        ? Mono.error(e)
                        : Mono.just("Error: " + e.getMessage()));
    }

    // Aikido Zen blocks (SSRF, outbound blocking, ...) must propagate as a server error
    // instead of being swallowed into a 200 response, same as any other Aikido block.
    private static boolean isAikidoBlock(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause.getClass().getName().startsWith("dev.aikido.agent_api.vulnerabilities")) {
                return true;
            }
        }
        return false;
    }
}
