import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

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