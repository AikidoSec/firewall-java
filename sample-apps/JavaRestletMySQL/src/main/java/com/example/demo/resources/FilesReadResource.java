package com.example.demo.resources;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FilesReadResource extends ServerResource {

    private static class FileRead {
        public String fileName;

        // Default constructor is needed for JSON deserialization
        public FileRead() {}

        public FileRead(String fileName) {
            this.fileName = fileName;
        }
    }

    @Post("json")
    public String read(String jsonInput) {
        FileRead fileData = parseJson(jsonInput);
        return readFile(fileData.fileName);
    }

    static String readFile(String fileName) {
        System.out.println("Reading file: " + fileName);
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

    private FileRead parseJson(String jsonInput) {
        // Simple JSON parsing (you can use a library like Jackson or Gson for more complex cases)
        String fileName = jsonInput.replaceAll(".*\"fileName\":\"([^\"]+)\".*", "$1");
        return new FileRead(fileName);
    }
}
