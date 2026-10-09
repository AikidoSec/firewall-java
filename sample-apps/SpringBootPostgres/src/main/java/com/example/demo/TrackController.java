package com.example.demo;

import dev.aikido.agent_api.Track;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/track")
public class TrackController {
    @PostMapping
    public String track() {
        Track.track("user.login_failed");
        return "OK";
    }
}
