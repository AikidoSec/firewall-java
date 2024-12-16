package dev.aikido.agent_api.vulnerabilities;

import java.io.Serializable;
import java.util.Map;

public class Attack implements Serializable {
    public final String operation;
    public final String kind;
    public final String source;
    public final String pathToPayload;
    public final Map<String, String> metadata;
    public final String payload;
    public final String stack;
    public Attack(String op, Vulnerabilities.Vulnerability vulnerability, String source, String pathToPayload, Map<String, String> metadata, String payload, String stack) {
        this.operation = op;
        this.kind = vulnerability.getKind();
        this.source = source;
        this.pathToPayload = pathToPayload;
        this.metadata = metadata;
        this.payload = payload;
        this.stack = stack;
    }
}
