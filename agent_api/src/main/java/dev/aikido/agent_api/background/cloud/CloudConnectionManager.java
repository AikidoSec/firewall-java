package dev.aikido.agent_api.background.cloud;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.Started;
import dev.aikido.agent_api.storage.ConfigStore;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.Statistics;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.background.users.Users;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.ratelimiting.RateLimiter;

import java.util.Optional;

import static dev.aikido.agent_api.helpers.env.Endpoints.getAikidoAPIEndpoint;

/**
 * Class contains logic for communication with Aikido Cloud : managing config, routes, calls to API, heartbeats
 */
public class CloudConnectionManager {
    // Constants:
    private static final int timeout = 10; // Timeout for HTTP requests to cloud

    private final ReportingApi api;
    private final String token;
    private final Routes routes;
    private final RateLimiter rateLimiter;
    private final Users users;
    private final Statistics stats;
    private final Hostnames hostnames;

    public CloudConnectionManager(boolean block, Token token, String serverless) {
        this(block, token, serverless, new ReportingApiHTTP(getAikidoAPIEndpoint(), timeout));
    }
    public CloudConnectionManager(boolean block, Token token, String serverless, ReportingApi api) {
        ConfigStore.updateBlocking(block);
        this.api = api;
        this.token = token.get();
        this.routes = new Routes(200); // Max size is 200 routes.
        this.rateLimiter = new RateLimiter(
                /*maxItems:*/ 5000, /*TTL in ms:*/ 120 * 60 * 1000 // 120 minutes
        );
        this.users = new Users();
        this.stats = new Statistics();
        this.hostnames = new Hostnames(5000); // max entry size is 5000
    }
    public void onStart() {
        reportEvent(/* event:*/ Started.get(this), /* update config:*/ true);
        // Fetch blocked lists using separate API call : 
        ConfigStore.updateFromAPIListsResponse(api.fetchBlockedLists(token));
    }
    public void reportEvent(APIEvent event, boolean updateConfig) {
        Optional<APIResponse> res = this.api.report(this.token, event);
        if (res.isPresent() && updateConfig) {
            ConfigStore.updateFromAPIResponse(res.get());
        }
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
    public Statistics getStats() { return stats; }

    public Hostnames getHostnames() {
        return hostnames;
    }
}
