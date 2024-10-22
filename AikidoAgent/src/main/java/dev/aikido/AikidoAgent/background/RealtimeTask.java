package dev.aikido.AikidoAgent.background;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.RealtimeAPI;
import dev.aikido.AikidoAgent.background.cloud.api.APIResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        logger.debug("Running realtime task, config last updated at: {}", configLastUpdatedAt);
        Optional<RealtimeAPI.ConfigResponse> res = new RealtimeAPI().getConfig(connectionManager.getToken());

        if(res.isPresent()) {
            long configAccordingToCloudUpdatedAt = res.get().configUpdatedAt();

            if (configLastUpdatedAt.isEmpty()) {
                configLastUpdatedAt = Optional.of(configAccordingToCloudUpdatedAt);
            }
            if(configLastUpdatedAt.get() < configAccordingToCloudUpdatedAt) {
                // Config was updated
                configLastUpdatedAt = Optional.of(configAccordingToCloudUpdatedAt); // Store new time of last update
                Optional<APIResponse> newConfig = connectionManager.getApi().fetchNewConfig(connectionManager.getToken(), /* Timeout in seconds: */ 3);
                newConfig.ifPresent(connectionManager::updateConfig);
                logger.debug("Config updated");
            }
        }
    }
}
