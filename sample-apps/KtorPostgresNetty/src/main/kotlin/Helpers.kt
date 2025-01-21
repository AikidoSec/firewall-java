package com.example


import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path

fun executeShellCommand(command: String): String {
    val output = StringBuilder()
    return try {
        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
        }
        process.waitFor()
        output.toString()
    } catch (e: IOException) {
        "Error: ${e.message}"
    } catch (e: InterruptedException) {
        "Error: ${e.message}"
    }
}

fun makeHttpRequest(urlString: String): String {
    val response = StringBuilder()
    return try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        BufferedReader(InputStreamReader(conn.inputStream)).use { br ->
            var inputLine: String?
            while (br.readLine().also { inputLine = it } != null) {
                response.append(inputLine).append("\n")
            }
        }
        response.toString()
    } catch (e: IOException) {
        "Error: ${e.message}"
    }
}

fun readFile(filePath: String): String {
    val content = StringBuilder()
    val file = Path.of("src/main/resources/blogs", filePath).toFile()
    return try {
        BufferedReader(FileReader(file)).use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                content.append(line).append("\n")
            }
        }
        content.toString()
    } catch (e: IOException) {
        "Error: ${e.message}"
    }
}
