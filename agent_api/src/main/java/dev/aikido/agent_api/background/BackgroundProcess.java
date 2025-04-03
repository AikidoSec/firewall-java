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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dev.aikido.agent_api.Config.heartbeatEveryXSeconds;
import static dev.aikido.agent_api.Config.pollingEveryXSeconds;
import static dev.aikido.agent_api.helpers.env.Endpoints.getAikidoAPIEndpoint;

public class BackgroundProcess extends Thread {
    private final static int API_TIMEOUT = 10; // 10 seconds
    private final Token token;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    public BackgroundProcess(String name, Token token) {
        super(name);
        this.token = token;
    }

    @Override
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


        // Schedule tasks using ScheduledExecutorService
        scheduler.scheduleAtFixedRate(new HeartbeatTask(api), heartbeatEveryXSeconds, heartbeatEveryXSeconds, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new RealtimeTask(realtimeApi, api), pollingEveryXSeconds, pollingEveryXSeconds, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new AttackQueueConsumerTask(api), 0, 2, TimeUnit.SECONDS);

        // one time check to report initial stats
        scheduler.schedule(new HeartbeatTask(api, true), 60, TimeUnit.SECONDS);
    }
}