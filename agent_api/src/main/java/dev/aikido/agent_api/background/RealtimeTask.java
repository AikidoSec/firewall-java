package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.RealtimeAPI;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

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
    private final CloudConnectionManager connectionManager;
    private Optional<Long> configLastUpdatedAt;
    public RealtimeTask(CloudConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.configLastUpdatedAt = Optional.empty();
    }
    @Override
    public void run() {
        logger.debug("Running realtime task, config last updated at: %s", configLastUpdatedAt);
        Optional<RealtimeAPI.ConfigResponse> res = new RealtimeAPI().getConfig(connectionManager.getToken());

        if(res.isPresent()) {
            long configAccordingToCloudUpdatedAt = res.get().configUpdatedAt();
            if(configLastUpdatedAt.isEmpty() || configLastUpdatedAt.get() < configAccordingToCloudUpdatedAt) {
                // Config was updated
                configLastUpdatedAt = Optional.of(configAccordingToCloudUpdatedAt); // Store new time of last update
                Optional<APIResponse> newConfig = connectionManager.getApi().fetchNewConfig(connectionManager.getToken(), /* Timeout in seconds: */ 3);
                newConfig.ifPresent(connectionManager.getConfig()::updateConfig);

                // Fetch blocked lists from separate API route :
                Optional<ReportingApi.APIListsResponse> blockedListsRes = connectionManager.getApi().fetchBlockedLists(connectionManager.getToken());
                connectionManager.getConfig().storeBlockedListsRes(blockedListsRes);

                logger.debug("Config updated");
            }
        }
    }
}
