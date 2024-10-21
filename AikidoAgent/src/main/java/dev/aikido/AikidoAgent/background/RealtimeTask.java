package dev.aikido.AikidoAgent.background;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.Realtime;
import dev.aikido.AikidoAgent.background.cloud.api.APIResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.TimerTask;

import static dev.aikido.AikidoAgent.helpers.UnixTimeMS.getUnixTimeMS;

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
        Optional<Realtime.ConfigResponse> res = new Realtime().getConfig(connectionManager.getToken());

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
