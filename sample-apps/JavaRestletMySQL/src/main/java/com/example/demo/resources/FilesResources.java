package com.example.demo.resources;

import org.restlet.data.MediaType;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FilesResources extends ServerResource {

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

    @Get("json")
    public String readCookie() {
        String filePath = getCookieValue("fpath", "");
        return readFile(filePath);
    }

    @Get("json")
    public String readHeaders() {
        String xpath = getRequest().getHeaders().getFirstValue("x-path", "");
        return readFile(xpath);
    }

    @Get("json")
    public String readHeaders2() {
        String[] xpath = getRequest().getHeaders().getValuesArray("x-path");
        return readFile(xpath.length > 0 ? xpath[0] : "");
    }

    private String getCookieValue(String cookieName, String defaultValue) {
        return getRequest().getCookies().getFirstValue(cookieName, defaultValue);
    }

    private static String readFile(String fileName) {
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
