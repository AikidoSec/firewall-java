package com.example.demo

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class HtmlController {

    @GetMapping("/")
    String home(Model model) {
        return "index" // This will resolve to src/main/resources/templates/index.html
    }

    @GetMapping("/create")
    String create(Model model) {
        return "create" // This will resolve to src/main/resources/templates/create.html
    }

    @GetMapping("/read_file")
    String read_file(Model model) {
        return "read" // This will resolve to src/main/resources/templates/read.html
    }

    @GetMapping("/request")
    String request(Model model) {
        return "request" // This will resolve to src/main/resources/templates/request.html
    }

    @GetMapping("/exec")
    String exec(Model model) {
        return "execute" // This will resolve to src/main/resources/templates/execute.html
    }

    @GetMapping("/pet_page/{id}")
    String pet_page(@PathVariable("id") Integer id) {
        return "pet_page"
    }

    @GetMapping("/benchmark")
    @ResponseBody
    String benchmark_route() throws InterruptedException {
        Thread.sleep(1);
        return "OK"
    }
}
