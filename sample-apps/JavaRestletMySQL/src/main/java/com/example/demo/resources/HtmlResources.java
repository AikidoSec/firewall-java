package com.example.demo.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public final class HtmlResources {
    public static class HomeResource extends ServerResource {
        @Get
        public String represent() {
            return "index"; // You can return HTML or JSON as needed
        }
    }

    public static class CreateResource extends ServerResource {
        @Get
        public String represent() {
            return "create";
        }
    }

    public static class CreateMariaDBResource extends ServerResource {
        @Get
        public String represent() {
            return "create_mariadb";
        }
    }

    public static class ReadFileResource extends ServerResource {
        @Get
        public String represent() {
            return "read";
        }
    }

    public static class RequestResource extends ServerResource {
        @Get
        public String represent() {
            return "request";
        }
    }

    public static class ExecResource extends ServerResource {
        @Get
        public String represent() {
            return "execute";
        }
    }

    public static class PetPageResource extends ServerResource {
        @Get
        public String represent() {
            String id = getAttribute("id");
            return "pet_page with id: " + id;
        }
    }

    public static class TestRateLimitingResource extends ServerResource {
        @Get
        public String represent() {
            return "index"; // Used in end2end tests
        }
    }

}
