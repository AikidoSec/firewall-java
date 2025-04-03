package com.example.demo;

import com.example.demo.models.Pet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HtmlController {

    @GetMapping("/")
    public String home(Model model) {
        return "index"; // This will resolve to src/main/resources/templates/index.html
    }

    @GetMapping("/create")
    public String create(Model model) {
        return "create"; // This will resolve to src/main/resources/templates/create.html
    }
    @GetMapping("/read_file")
    public String read_file(Model model) {
        return "read"; // This will resolve to src/main/resources/templates/read.html
    }
    @GetMapping("/request")
    public String request(Model model) {
        return "request"; // This will resolve to src/main/resources/templates/request.html
    }
    @GetMapping("/exec")
    public String exec(Model model) {
        return "execute"; // This will resolve to src/main/resources/templates/execute.html
    }

    @GetMapping("/pet_page/{id}")
    public String pet_page(@PathVariable("id") Integer id) {
        return "pet_page";
    }

    @GetMapping("/benchmark")
    @ResponseBody
    public String benchmark_route() throws InterruptedException {
        Thread.sleep(1); // sleep 1ms
        return "OK";
    }
}
