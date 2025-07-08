package com.example.demo.resources;

import org.restlet.data.MediaType;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestsResources extends ServerResource {

    private static class RequestsGet {
        public String url;

        // Default constructor is needed for JSON deserialization
        public RequestsGet() {}

        public RequestsGet(String url) {
            this.url = url;
        }
    }

    @Post("json")
    public String handlePost(String jsonInput) throws IOException {
        RequestsGet requestsGet = parseJson(jsonInput);
        String url = requestsGet.url;
        System.out.println("Making request to: " + url);
        return sendGetRequest(url);
    }

    private RequestsGet parseJson(String jsonInput) {
        // Simple JSON parsing (you can use a library like Jackson or Gson for more complex cases)
        String url = jsonInput.replaceAll(".*\"url\":\"([^\"]+)\".*", "$1");
        return new RequestsGet(url);
    }

    private static String sendGetRequest(String urlString) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to GET
        connection.setRequestMethod("GET");

        // Get the response code
        int responseCode = connection.getResponseCode();

        // If the response code is 200 (HTTP_OK), read the response
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
        } else {
            System.out.println("GET request failed: " + responseCode);
        }

        // Disconnect the connection
        connection.disconnect();

        return result.toString();
    }
}
