package com.example.demo;

import com.example.demo.resources.*;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RestletApplication extends Application {

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        router.attach("/static/{fileName}", ClasspathFileResources.class);

        router.attach("/", HtmlResources.HomeResource.class);
        router.attach("/create", HtmlResources.CreateResource.class);
        router.attach("/create/mariadb", HtmlResources.CreateMariaDBResource.class);
        router.attach("/read_file", HtmlResources.ReadFileResource.class);
        router.attach("/request", HtmlResources.RequestResource.class);
        router.attach("/exec", HtmlResources.ExecResource.class);
        router.attach("/pet_page/{id}", HtmlResources.PetPageResource.class);
        router.attach("/test_ratelimiting_1", HtmlResources.TestRateLimitingResource.class);
        router.attach("/api/requests/get", RequestsResources.class);
        router.attach("/api/pets", PetsResources.class);
        router.attach("/api/files", FilesResources.class);
        router.attach("/api/commands/execute", CommandResources.class);
        return router;
    }
}
