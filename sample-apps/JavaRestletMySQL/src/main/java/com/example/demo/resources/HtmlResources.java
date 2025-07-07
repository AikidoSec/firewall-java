package com.example.demo.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public final class HtmlResources {
    public static class HomeResource extends ServerResource {
        @Get
        public String represent() {
            // Redirect to a static HTML page
            getResponse().redirectSeeOther("/static/index.html");
            return "Redirecting to index page";
        }
    }

    public static class CreateResource extends ServerResource {
        @Get
        public String represent() {
            // Redirect to a static HTML page
            getResponse().redirectSeeOther("/static/create.html");
            return "Redirecting to create page";
        }
    }

    public static class ReadFileResource extends ServerResource {
        @Get
        public String represent() {
            // Redirect to a static HTML page
            getResponse().redirectSeeOther("/static/read.html");
            return "Redirecting to read file page";
        }
    }

    public static class RequestResource extends ServerResource {
        @Get
        public String represent() {
            // Redirect to a static HTML page
            getResponse().redirectSeeOther("/static/request.html");
            return "Redirecting to request page";
        }
    }

    public static class ExecResource extends ServerResource {
        @Get
        public String represent() {
            // Redirect to a static HTML page
            getResponse().redirectSeeOther("/static/execute.html");
            return "Redirecting to execute page";
        }
    }

    public static class PetPageResource extends ServerResource {
        @Get
        public String represent() {
            String id = getAttribute("id");
            // Redirect to a static HTML page with the ID as a parameter
            getResponse().redirectSeeOther("/static/pet_page.html?id=" + id);
            return "Redirecting to pet page with id: " + id;
        }
    }

    public static class TestRateLimitingResource extends ServerResource {
        @Get
        public String represent() {
            // Redirect to a static HTML page
            getResponse().redirectSeeOther("/static/index.html");
            return "Redirecting to index page for end-to-end tests";
        }
    }
}
