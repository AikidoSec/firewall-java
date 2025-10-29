package dev.aikido.agent_api.vulnerabilities;

import com.google.gson.Gson;
import dev.aikido.agent_api.context.User;

import java.util.Map;

public class Attack {
    public final String operation;
    public final String kind;
    public final String source;
    public final String pathToPayload;
    public final Map<String, String> metadata;
    public final String payload;
    public final String stack;
    public final User user;
    public Attack(String op, Vulnerabilities.Vulnerability vulnerability, String source, String pathToPayload, Map<String, String> metadata, String payload, String stack, User user) {
        this.operation = op;
        this.kind = vulnerability.getKind();
        this.source = source;
        this.pathToPayload = pathToPayload;
        this.metadata = metadata;
        this.payload = payload;
        this.stack = stack;
        this.user = user;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
