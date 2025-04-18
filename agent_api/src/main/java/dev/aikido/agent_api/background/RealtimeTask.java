package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.RealtimeAPI;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.ServiceConfigStore;

import java.util.Optional;
import java.util.TimerTask;

/**
 * This tasks runs every x seconds to poll for config changes at the realtime endpoint,
 * normally runtime.aikido.dev but can be specified with AIKIDO_REALTIME_ENDPOINT.
 * If it notices that the config was updated (i.e. time of last change is more recent) it will
 * fetch the new config from the Zen API (guard.aikido.dev).
 */
public class RealtimeTask extends TimerTask {
    private static final Logger logger = LogManager.getLogger(RealtimeTask.class);
    private final RealtimeAPI realtimeApi;
    private final ReportingApiHTTP reportingApi;
    private Optional<Long> configLastUpdatedAt;
    public RealtimeTask(RealtimeAPI realtimeApi, ReportingApiHTTP reportingApi) {
        this.realtimeApi = realtimeApi;
        this.reportingApi = reportingApi;
        this.configLastUpdatedAt = Optional.empty();
    }
    @Override
    public void run() {
        logger.debug("Running realtime task, config last updated at: %s", configLastUpdatedAt);
        Optional<RealtimeAPI.ConfigResponse> res = realtimeApi.getConfig();

        if(res.isPresent()) {
            long configAccordingToCloudUpdatedAt = res.get().configUpdatedAt();
            if(configLastUpdatedAt.isEmpty() || configLastUpdatedAt.get() < configAccordingToCloudUpdatedAt) {
                // Store new time of last update
                configLastUpdatedAt = Optional.of(configAccordingToCloudUpdatedAt);

                // fetch the new config
                Optional<APIResponse> newConfig = reportingApi.fetchNewConfig();
                newConfig.ifPresent(ServiceConfigStore.getConfig()::updateConfig);

                // Fetch blocked lists from separate API route
                Optional<ReportingApi.APIListsResponse> blockedListsRes = reportingApi.fetchBlockedLists();
                blockedListsRes.ifPresent(ServiceConfigStore::updateFromAPIListsResponse);

                logger.debug("Config updated");
            }
        }
    }
}
