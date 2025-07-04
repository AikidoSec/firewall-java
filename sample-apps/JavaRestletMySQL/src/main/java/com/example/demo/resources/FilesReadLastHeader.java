package com.example.demo.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import static com.example.demo.resources.FilesReadResource.readFile;

public class FilesReadLastHeader extends ServerResource {
    @Get("json")
    public String lastHeader() {
        String[] xpaths = getRequest().getHeaders().getValuesArray("x-path");
        return readFile(xpaths[xpaths.length - 1]);
    }
}
