package dev.aikido.AikidoAgent.vulnerabilities;

import com.google.gson.Gson;
import dev.aikido.AikidoAgent.background.utilities.IPCClient;

import java.util.Map;

public class Injection {
    public final String operation;
    public final String kind;
    public final String source;
    public final String pathToPayload;
    public final Map<String, String> metadata;
    public final String payload;
    Injection(String op, Attacks.Attack attack, String source, String pathToPayload, Map<String, String> metadata, String payload) {
        this.operation = op;
        this.kind = attack.getKind();
        this.source = source;
        this.pathToPayload = pathToPayload;
        this.metadata = metadata;
        this.payload = payload;
    }

    public void reportOverIPC(IPCClient ipcClient) {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        ipcClient.sendData("ATTACK$" + json);
    }
}
