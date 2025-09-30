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

    @GetMapping("/pet_page/{id}")
    public String pet_page(@PathVariable("id") Integer id) {
        return "pet_page";
    }
}
