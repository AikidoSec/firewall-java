package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.RealtimeAPI;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.Started;
import dev.aikido.agent_api.helpers.env.BlockingEnv;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.storage.ServiceConfigStore;

import java.util.Optional;
import java.util.Timer;

import static dev.aikido.agent_api.Config.heartbeatEveryXSeconds;
import static dev.aikido.agent_api.Config.pollingEveryXSeconds;
import static dev.aikido.agent_api.helpers.env.Endpoints.getAikidoAPIEndpoint;

public class BackgroundProcess extends Thread {
    private final Token token;
    private final static int API_TIMEOUT = 10; // 10 seconds
    public BackgroundProcess(String name, Token token) {
        super(name);
        this.token = token;
    }

    public void run() {
        if (!Thread.currentThread().isDaemon() && token == null) {
            return; // Can only run if thread is daemon and token needs to be defined.
        }
        ServiceConfigStore.updateBlocking(new BlockingEnv().getValue());
        ReportingApiHTTP api = new ReportingApiHTTP(getAikidoAPIEndpoint(), API_TIMEOUT, token);
        RealtimeAPI realtimeApi = new RealtimeAPI(token);

        // make api calls on start
        Optional<APIResponse> res = api.report(Started.get());
        res.ifPresent(ServiceConfigStore::updateFromAPIResponse);
        // Fetch blocked lists using separate API call
        Optional<ReportingApi.APIListsResponse> listsRes = api.fetchBlockedLists();
        listsRes.ifPresent(ServiceConfigStore::updateFromAPIListsResponse);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(
                new HeartbeatTask(api), // Heartbeat task: Sends statistics, route data, etc.
                heartbeatEveryXSeconds * 1000, // Delay before first execution in milliseconds
                heartbeatEveryXSeconds * 1000 // Interval in milliseconds
        );
        timer.scheduleAtFixedRate(
                new RealtimeTask(realtimeApi, api), // Realtime task: makes sure config updates happen fast
                pollingEveryXSeconds * 1000, // Delay before first execution in milliseconds
                pollingEveryXSeconds * 1000 // Interval in milliseconds
        );
        timer.scheduleAtFixedRate(
                new AttackQueueConsumerTask(api), // Consumes from the attack queue (so attacks are reported in background)
                /* delay: */ 0, /* interval: */ 2 * 1000 // Clear queue every 2 seconds
        );
        // Report initial statistics if those were not received
        timer.schedule(
                new HeartbeatTask(api, true /* Check for initial statistics */), // Initial heartbeat task
                60_000 // Delay in ms
        );
    }
}