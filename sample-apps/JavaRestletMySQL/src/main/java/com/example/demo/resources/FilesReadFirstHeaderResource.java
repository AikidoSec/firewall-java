package com.example.demo.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import static com.example.demo.resources.FilesReadResource.readFile;

public class FilesReadFirstHeaderResource extends ServerResource {
    @Get("json")
    public String firstHeader() {
        String xpath = getRequest().getHeaders().getFirstValue("x-path", "");
        return readFile(xpath);
    }
}
