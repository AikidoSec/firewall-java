package com.example.demo

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/files") // Base URL for all routes in this controller
class FilesController {

    @PostMapping(path = "/read",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    String read(@RequestParam String fileName) {
        println("Reading file: " + fileName)
        File specifiedFile = new File(fileName)
        StringBuilder stringBuilder = new StringBuilder()
        Scanner myReader = null
        try {
            myReader = new Scanner(specifiedFile)
            while (myReader.hasNextLine()) {
                stringBuilder.append(myReader.nextLine())
                stringBuilder.append('\n')
            }
            myReader.close()
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e)
        }
        return stringBuilder.toString()
    }
}
