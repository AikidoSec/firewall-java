package com.example.demo;

import com.example.demo.repositories.UserRepository;
import com.example.demo.services.VulnerableLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class VulnerableLoginController {

    @Autowired
    private VulnerableLoginService vulnerableLoginService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/vulnerableLogin")
    public String vulnerableLogin() {
        return "login";
    }

    @PostMapping("/vulnerableLogin")
    public ResponseEntity<Map<String, Object>> vulnerableLoginSubmit(@RequestBody Map<String, Object> loginData) {
        String email = (String) loginData.get("email");
        Object password = loginData.get("password");

        Map<String, Object> response = new HashMap<>();

        if (vulnerableLoginService.authenticate(email, password)) {
            response.put("userDetails", userRepository.findByEmail(email));
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Invalid email or password");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
