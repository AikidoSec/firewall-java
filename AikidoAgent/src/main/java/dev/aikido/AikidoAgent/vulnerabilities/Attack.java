package dev.aikido.AikidoAgent.vulnerabilities;

import java.util.Map;

public class Attack {
    public final String operation;
    public final String kind;
    public final String source;
    public final String pathToPayload;
    public final Map<String, String> metadata;
    public final String payload;
    Attack(String op, Vulnerabilities.Vulnerability vulnerability, String source, String pathToPayload, Map<String, String> metadata, String payload) {
        this.operation = op;
        this.kind = vulnerability.getKind();
        this.source = source;
        this.pathToPayload = pathToPayload;
        this.metadata = metadata;
        this.payload = payload;
    }
}
