package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

@RestController
@RequestMapping("/api/requests") // Base URL for all routes in this controller
public class RequestsController {
    @PostMapping(path = "/get",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String get(@RequestParam(name="url") String url2) throws IOException, InterruptedException {
        System.out.println("Making request to: "+ url2);
        return sendGetRequest2(url2);
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
    private static String sendGetRequest2(String urlString) throws IOException, InterruptedException {
        StringBuilder result = new StringBuilder();
        var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL) // Allow normal redirects
                .build();

        var request = HttpRequest
                .newBuilder(URI.create(urlString))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
