package dev.aikido.agent_api.background.cloud;

import dev.aikido.agent_api.storage.*;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.Started;
import dev.aikido.agent_api.helpers.env.Token;

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

    public CloudConnectionManager(boolean block, Token token) {
        this(block, token, new ReportingApiHTTP(getAikidoAPIEndpoint(), timeout));
    }
    public CloudConnectionManager(boolean block, Token token, ReportingApi api) {
        ServiceConfigStore.updateBlocking(block);
        this.api = api;
        this.token = token.get();
    }
    public void onStart() {
        reportEvent(/* event:*/ Started.get(this), /* update config:*/ true);
        // Fetch blocked lists using separate API call :
        Optional<ReportingApi.APIListsResponse> res = this.api.fetchBlockedLists(token);
        if (res.isPresent()) {
            ServiceConfigStore.updateFromAPIListsResponse(res.get());
        }
    }
    public void reportEvent(APIEvent event, boolean updateConfig) {
        Optional<APIResponse> res = this.api.report(this.token, event);
        if (res.isPresent() && updateConfig) {
            ServiceConfigStore.updateFromAPIResponse(res.get());
        }
    }
    public GetManagerInfo.ManagerInfo getManagerInfo() {
        return GetManagerInfo.getManagerInfo(this);
    }
    public String getToken() {
        return token;
    }
    public ReportingApiHTTP getApi() {
        return (ReportingApiHTTP) api;
    }
}
