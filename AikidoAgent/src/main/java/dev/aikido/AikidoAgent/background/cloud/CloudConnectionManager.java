package dev.aikido.AikidoAgent.background.cloud;

import dev.aikido.AikidoAgent.background.HeartbeatTask;
import dev.aikido.AikidoAgent.background.RealtimeTask;
import dev.aikido.AikidoAgent.background.cloud.api.APIResponse;
import dev.aikido.AikidoAgent.background.cloud.api.ReportingApi;
import dev.aikido.AikidoAgent.background.cloud.api.ReportingApiHTTP;
import dev.aikido.AikidoAgent.background.cloud.api.events.APIEvent;
import dev.aikido.AikidoAgent.background.cloud.api.events.DetectedAttack;
import dev.aikido.AikidoAgent.background.cloud.api.events.Started;
import dev.aikido.AikidoAgent.background.routes.Routes;
import dev.aikido.AikidoAgent.helpers.env.Token;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static dev.aikido.AikidoAgent.helpers.env.Endpoints.getAikidoAPIEndpoint;

/**
 * Class contains logic for communication with Aikido Cloud : managing config, routes, calls to API, heartbeats
 */
public class CloudConnectionManager {
    // Timeout for HTTP requests to server :
    private static final int timeout = 10;
    private static final int heartbeatEveryXSeconds = 600; // 10 minutes
    private static final int pollingEveryXSeconds = 60; // Check for realtime config changes every 1 minute
    private boolean blockingEnabled;
    private final String serverless;
    private final ReportingApi api;
    private final String token;
    private final Routes routes;
    public CloudConnectionManager(boolean block, Token token, String serverless) {
        if (serverless != null && serverless.isEmpty()) {
            throw new IllegalArgumentException("Serverless cannot be an empty string");
        }
        this.blockingEnabled = block;
        this.serverless = serverless;
        this.api = new ReportingApiHTTP(getAikidoAPIEndpoint());
        this.token = token.get();
        this.routes = new Routes(200); // Max size is 200 routes.
    }
    public void onStart() {
        reportEvent(/* event:*/ Started.get(this), /* update config:*/ true);
        // Start heartbeat :
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(
                new HeartbeatTask(this), // Create a heartbeat task with this context (CloudConnectionManager)
                heartbeatEveryXSeconds * 1000, // Delay before first execution in milliseconds
                heartbeatEveryXSeconds * 1000 // Interval in milliseconds
        );
        timer.scheduleAtFixedRate(
                new RealtimeTask(this), // Create a realtime task with this context (CloudConnectionManager)
                pollingEveryXSeconds * 1000, // Delay before first execution in milliseconds
                pollingEveryXSeconds * 1000 // Interval in milliseconds
        );
    }
    public void reportEvent(APIEvent event, boolean updateConfig) {
        Optional<APIResponse> res = this.api.report(this.token, event, timeout);
        if (res.isPresent() && updateConfig) {
            updateConfig(res.get());
        }
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
    public Routes getRoutes() {
        return routes;
    }
    public String getToken() {
        return token;
    }
    public ReportingApiHTTP getApi() {
        return (ReportingApiHTTP) api;
    }
}
