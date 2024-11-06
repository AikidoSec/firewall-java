package dev.aikido.agent_api.background.cloud;

import dev.aikido.agent_api.background.HeartbeatTask;
import dev.aikido.agent_api.background.RealtimeTask;
import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.background.cloud.api.events.Started;
import dev.aikido.agent_api.background.routes.Routes;
import dev.aikido.agent_api.background.users.Users;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.ratelimiting.RateLimiter;

import java.util.Optional;
import java.util.Timer;

import static dev.aikido.agent_api.helpers.env.Endpoints.getAikidoAPIEndpoint;

/**
 * Class contains logic for communication with Aikido Cloud : managing config, routes, calls to API, heartbeats
 */
public class CloudConnectionManager {
    // Constants:
    private static final int timeout = 10; // Timeout for HTTP requests to cloud

    private final ServiceConfiguration config;
    private final ReportingApi api;
    private final String token;
    private final Routes routes;
    private final RateLimiter rateLimiter;
    private final Users users;
    public CloudConnectionManager(boolean block, Token token, String serverless) {
        this.config = new ServiceConfiguration(block, serverless);
        this.api = new ReportingApiHTTP(getAikidoAPIEndpoint());
        this.token = token.get();
        this.routes = new Routes(200); // Max size is 200 routes.
        this.rateLimiter = new RateLimiter(
                /*maxItems:*/ 5000, /*TTL in ms:*/ 120 * 60 * 1000 // 120 minutes
        );
        this.users = new Users();
    }
    public void onStart() {
        reportEvent(/* event:*/ Started.get(this), /* update config:*/ true);
    }
    public void reportEvent(APIEvent event, boolean updateConfig) {
        Optional<APIResponse> res = this.api.report(this.token, event, timeout);
        if (res.isPresent() && updateConfig) {
            config.updateConfig(res.get());
        }
    }
    public boolean shouldBlock() {
        return this.config.isBlockingEnabled();
    }

    public ServiceConfiguration getConfig() {
        return this.config;
    }
    public GetManagerInfo.ManagerInfo getManagerInfo() {
        return GetManagerInfo.getManagerInfo(this);
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

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public Users getUsers() {
        return users;
    }
}
