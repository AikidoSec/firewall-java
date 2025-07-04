package com.example.demo.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import static com.example.demo.resources.FilesReadResource.readFile;

public class FilesReadCookieResource extends ServerResource {
    private String getCookieValue(String cookieName, String defaultValue) {
        return getRequest().getCookies().getFirstValue(cookieName, defaultValue);
    }

    @Get("json")
    public String readCookie() {
        String filePath = getCookieValue("fpath", "");
        return readFile(filePath);
    }
}
