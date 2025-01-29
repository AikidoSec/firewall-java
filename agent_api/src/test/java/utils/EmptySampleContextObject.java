package utils;

import dev.aikido.agent_api.context.ContextObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EmptySampleContextObject extends ContextObject {
    public EmptySampleContextObject() {
        this.method = "GET";
        this.source = "web";
        this.url = "https://example.com/api/resource";
        this.route = "/api/resource";
        this.remoteAddress = "192.168.1.1";
        this.headers = new HashMap<>();
        this.query = new HashMap<>();
        this.cookies = new HashMap<>();
        this.redirectStartNodes = new ArrayList<>();
    }

    public EmptySampleContextObject(String argument) {
        this();
        this.query.put("arg", List.of(argument));
    }
}
