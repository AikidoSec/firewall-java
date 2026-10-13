package com.example.probe;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/")
public class HomeController {

    @Get
    public String index() {
        return "OK";
    }

    @Get("/test_ratelimiting_1")
    public String testRatelimiting1() {
        return "OK";
    }
}
