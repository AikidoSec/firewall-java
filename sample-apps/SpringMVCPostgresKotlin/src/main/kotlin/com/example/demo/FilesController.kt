package com.example.demo

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.FileNotFoundException

@RestController
@RequestMapping("/api/files") // Base URL for all routes in this controller
class FilesController {

    @PostMapping(
        path = ["/read"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun read(@RequestParam fileName: String): String {
        println("Reading file: $fileName")
        val specifiedFile = File(fileName)
        val stringBuilder = StringBuilder()

        return try {
            specifiedFile.forEachLine { line ->
                stringBuilder.append(line).append('\n')
            }
            stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            throw RuntimeException("File not found: $fileName", e)
        }
    }
}
