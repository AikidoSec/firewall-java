package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@RestController
@RequestMapping("/api/requests") // Base URL for all routes in this controller
public class RequestsController {
    private record RequestsGet(String url) {}

    @PostMapping(path = "/get",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String get(@RequestBody RequestsGet requestsGet) throws IOException {
        String url = requestsGet.url;
        System.out.println("Making request to: "+ url);
        return sendGetRequest(url);
    }

    private static String sendGetRequest(String urlString) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true); // Allow for redirects.

        // Set the request method to GET
        connection.setRequestMethod("GET");

        // Get the response code
        int responseCode = connection.getResponseCode();

        // If the response code is 200 (HTTP_OK), read the response
        if (responseCode != -1 && responseCode < 400) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
        } else {
            System.out.println(connection.getResponseMessage());
            System.out.println("GET request failed: " + responseCode);
        }

        // Disconnect the connection
        connection.disconnect();

        return result.toString();
    }
}
