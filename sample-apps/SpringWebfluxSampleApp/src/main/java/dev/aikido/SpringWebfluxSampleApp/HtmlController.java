package dev.aikido.SpringWebfluxSampleApp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Controller
public class HtmlController {

    @GetMapping("/")
    public Mono<String> home(Model model) {
        return Mono.just("index"); // This will resolve to src/main/resources/templates/index.html
    }

    @GetMapping("/create")
    public Mono<String> create(Model model) {
        return Mono.just("create"); // This will resolve to src/main/resources/templates/create.html
    }

    @GetMapping("/pet_page/{id}")
    public Mono<String> pet_page(@PathVariable("id") Integer id) {
        return Mono.just("pet_page"); // This will resolve to src/main/resources/templates/pet_page.html
    }

    @GetMapping("/benchmark_1ms")
    public Mono<String> benchmark_route() {
        return Mono.delay(Duration.ofMillis(1)) // Sleep 1 millisecond in a non-blocking way
                .then(Mono.just("OK"));
    }
}
