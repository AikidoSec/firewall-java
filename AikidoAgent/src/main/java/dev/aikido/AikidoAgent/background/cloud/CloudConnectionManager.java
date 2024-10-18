package dev.aikido.AikidoAgent.background.cloud;

import dev.aikido.AikidoAgent.background.cloud.api.APIResponse;
import dev.aikido.AikidoAgent.background.cloud.api.ReportingApi;
import dev.aikido.AikidoAgent.background.cloud.api.ReportingApiHTTP;
import dev.aikido.AikidoAgent.background.cloud.api.events.DetectedAttack;
import dev.aikido.AikidoAgent.background.cloud.api.events.Started;
import dev.aikido.AikidoAgent.helpers.env.Token;

import java.util.Optional;

/**
 * Class contains logic for communication with Aikido Cloud : managing config, routes, calls to API, heartbeats
 */
public class CloudConnectionManager {
    // Timeout for HTTP requests to server :
    private final int timeout = 10;
    private boolean blockingEnabled;
    private String serverless;
    private ReportingApi api;
    private final String token;
    public CloudConnectionManager(boolean block, Token token, String serverless) {
        if (serverless != null && serverless.isEmpty()) {
            throw new IllegalArgumentException("Serverless cannot be an empty string");
        }
        this.blockingEnabled = block;
        this.serverless = serverless;
        this.api = new ReportingApiHTTP("https://guard.aikido.dev/");
        this.token = token.get();
    }
    public void onStart() {
        Optional< APIResponse> res = this.api.report(this.token, Started.get(this), this.timeout);
        res.ifPresent(this::updateConfig);
    }
    public void onDetectedAttack(DetectedAttack.DetectedAttackEvent event) {
        this.api.report(this.token, event, this.timeout);
    }
    public boolean shouldBlock() {
        return this.blockingEnabled;
    }

    public String getServerless() {
        return serverless;
    }
    public GetManagerInfo.ManagerInfo getManagerInfo() {
        return GetManagerInfo.getManagerInfo(this);
    }
    public void updateConfig(APIResponse apiResponse) {
        if (!apiResponse.success()) {
            return;
        }
        this.blockingEnabled = apiResponse.block();
    }
}
