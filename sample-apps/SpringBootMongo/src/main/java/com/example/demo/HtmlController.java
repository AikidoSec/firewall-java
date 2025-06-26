package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlController {

    @GetMapping("/")
    public String home(Model model) {
        return "index"; // This will resolve to src/main/resources/templates/index.html
    }

    @GetMapping("/test_ratelimiting_1")
    public String test_ratelimiting_1() {
        // This route is used in end2end tests
        return "index";
    }
}
