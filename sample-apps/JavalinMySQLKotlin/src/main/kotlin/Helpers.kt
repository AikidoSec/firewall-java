import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path

fun loadHtmlFromFile(filePath: String): String {
    val content = StringBuilder()
    try {
        BufferedReader(FileReader(File(filePath))).use { br ->
            var line: String?
            while ((br.readLine().also { line = it }) != null) {
                content.append(line).append("\n")
            }
        }
    } catch (e: IOException) {
        return "Error loading HTML file: " + e.message
    }
    return content.toString()
}
fun executeShellCommand(command: String?): String {
    val output = java.lang.StringBuilder()
    try {
        val process = Runtime.getRuntime().exec("echo '$command'")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while ((reader.readLine().also { line = it }) != null) {
            output.append(line).append("\n")
        }
        process.waitFor()
    } catch (e: IOException) {
        output.append("Error: ").append(e.message)
    } catch (e: InterruptedException) {
        output.append("Error: ").append(e.message)
    }
    return output.toString()
}

fun makeHttpRequest(urlString: String?): String {
    val response = java.lang.StringBuilder()
    try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        val `in` = BufferedReader(InputStreamReader(conn.inputStream))
        var inputLine: String?
        while ((`in`.readLine().also { inputLine = it }) != null) {
            response.append(inputLine).append("\n")
        }
        `in`.close()
    } catch (e: IOException) {
        response.append("Error: ").append(e.message)
    }
    return response.toString()
}

fun readFile(filePath: String?): String {
    val content = java.lang.StringBuilder()
    val file = Path.of("src/main/resources/blogs", filePath).toFile()
    try {
        BufferedReader(FileReader(file)).use { br ->
            var line: String?
            while ((br.readLine().also { line = it }) != null) {
                content.append(line).append("\n")
            }
        }
    } catch (e: IOException) {
        content.append("Error: ").append(e.message)
    }
    return content.toString()
}