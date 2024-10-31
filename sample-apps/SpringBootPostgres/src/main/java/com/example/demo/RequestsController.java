package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@RestController
@RequestMapping("/api/requests") // Base URL for all routes in this controller
public class RequestsController {
    private record RequestsGet(String url) {}

    @PostMapping(path = "/get",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String get(@RequestBody RequestsGet requestsGet) {
        String url = requestsGet.url;
        System.out.println("Making request to: "+ url);
        return "";
    }
}
