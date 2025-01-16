package com.example.demo

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@RestController
@RequestMapping("/api/requests") // Base URL for all routes in this controller
class RequestsController {

    @PostMapping(
        path = ["/get"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    @Throws(IOException::class, InterruptedException::class)
    fun get(@RequestParam(name = "url") url: String): String {
        println("Making request to: $url")
        return sendGetRequest2(url)
    }

    private fun sendGetRequest(urlString: String): String {
        val result = StringBuilder()
        val url = java.net.URL(urlString)
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.instanceFollowRedirects = true // Allow for redirects.

        // Set the request method to GET
        connection.requestMethod = "GET"

        // Get the response code
        val responseCode = connection.responseCode

        // If the response code is 200 (HTTP_OK), read the response
        if (responseCode != -1 && responseCode < 400) {
            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    result.append(line)
                }
            }
        } else {
            println(connection.responseMessage)
            println("GET request failed: $responseCode")
        }

        // Disconnect the connection
        connection.disconnect()

        return result.toString()
    }

    private fun sendGetRequest2(urlString: String): String {
        val result = StringBuilder()
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL) // Allow normal redirects
            .build()

        val request = HttpRequest.newBuilder(URI.create(urlString)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}
