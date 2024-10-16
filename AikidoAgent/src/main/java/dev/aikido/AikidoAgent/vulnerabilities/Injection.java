package dev.aikido.AikidoAgent.vulnerabilities;

import com.google.gson.Gson;
import dev.aikido.AikidoAgent.background.utilities.IPCClient;

public class Injection {
    public final String operation;
    public final String kind;
    public final String source;
    public final String pathToPayload;
    Injection(String op, Attacks.Attack attack, String source, String pathToPayload) {
        this.operation = op;
        this.kind = attack.getKind();
        this.source = source;
        this.pathToPayload = pathToPayload;
    }

    public void reportOverIPC(IPCClient ipcClient) {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        ipcClient.sendData("ATTACK$" + json);
    }
}
