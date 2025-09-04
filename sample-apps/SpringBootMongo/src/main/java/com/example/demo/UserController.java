package com.example.demo;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/create")
    public String index(Model model) {
        model.addAttribute("user", new User());
        return "create_user";
    }

    @PostMapping("/createUser")
    public String createUser(@ModelAttribute User user, Model model) {
        userRepository.save(user);
        model.addAttribute("message", "User created successfully!");
        return "create_user";
    }
}
