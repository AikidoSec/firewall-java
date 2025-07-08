package com.example.demo.resources;

import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.io.InputStream;

public class ClasspathFileResources extends ServerResource {
    @Get
    public StringRepresentation represent() {
        String fileName = getAttribute("fileName");
        InputStream inputStream = getClass().getResourceAsStream("/templates/" + fileName);

        if (inputStream == null) {
            return new StringRepresentation("File not found", MediaType.TEXT_PLAIN);
        }

        try {
            // Read the content of the file
            String content = new String(inputStream.readAllBytes());
            return new StringRepresentation(content, MediaType.TEXT_HTML);
        } catch (IOException e) {
            return new StringRepresentation("Error reading file: " + e.getMessage(), MediaType.TEXT_PLAIN);
        }
    }
}
