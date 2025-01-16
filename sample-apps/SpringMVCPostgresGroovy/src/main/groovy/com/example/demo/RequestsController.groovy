package com.example.demo

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@RestController
@RequestMapping("/api/requests") // Base URL for all routes in this controller
class RequestsController {

    @PostMapping(path = "/get",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    String get(@RequestParam(name = "url") String url2) throws IOException, InterruptedException {
        println("Making request to: " + url2)
        return sendGetRequest2(url2)
    }

    private static String sendGetRequest(String urlString) throws IOException {
        StringBuilder result = new StringBuilder()
        URL url = new URL(urlString)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        connection.setInstanceFollowRedirects(true) // Allow for redirects.

        // Set the request method to GET
        connection.setRequestMethod("GET")

        // Get the response code
        int responseCode = connection.getResponseCode()

        // If the response code is 200 (HTTP_OK), read the response
        if (responseCode != -1 && responseCode < 400) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
            String line
            while ((line = reader.readLine()) != null) {
                result.append(line)
            }
            reader.close()
        } else {
            println(connection.getResponseMessage())
            println("GET request failed: " + responseCode)
        }

        // Disconnect the connection
        connection.disconnect()

        return result.toString()
    }

    private static String sendGetRequest2(String urlString) throws IOException, InterruptedException {
        StringBuilder result = new StringBuilder()
        def client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL) // Allow normal redirects
                .build()

        def request = HttpRequest
                .newBuilder(URI.create(urlString))
                .build()
        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}
