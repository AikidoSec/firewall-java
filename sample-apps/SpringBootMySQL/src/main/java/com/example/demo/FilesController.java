package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@RestController
@RequestMapping("/api/files") // Base URL for all routes in this controller
public class FilesController {
    private record FileRead(String fileName) {}
    @PostMapping(path = "/read",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String read(@RequestBody FileRead fileData) {
        return readFile(fileData.fileName);
    }
    @GetMapping("/read_cookie")
    public String readCookie(@CookieValue(value = "fpath", defaultValue = "") String filePath) {
        return readFile(filePath);
    }
    @GetMapping("/read_header_1")
    public String readHeaders(@RequestHeader(value = "x-path", defaultValue = "") String xpath) {
        return readFile(xpath);
    }
    @GetMapping("/read_header_2")
    public String readHeaders2(@RequestHeader(value = "x-path", defaultValue = "") String[] xpath) {
        return readFile(xpath[0]);
    }
    private static String readFile(String fileName) {
        System.out.println("Reading file: "+ fileName);
        File specifiedFile = new File(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        Scanner myReader = null;
        try {
            myReader = new Scanner(specifiedFile);
            while (myReader.hasNextLine()) {
                stringBuilder.append(myReader.nextLine());
                stringBuilder.append('\n');
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }
}