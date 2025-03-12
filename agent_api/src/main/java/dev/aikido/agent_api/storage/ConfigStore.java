package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public final class ConfigStore {
    private ConfigStore() {
    }

    private static final Logger logger = LogManager.getLogger(ConfigStore.class);
    private static final Configuration config = new Configuration();
    private static final ReentrantLock mutex = new ReentrantLock();

    public static Configuration getConfig() {
        mutex.lock();
        Configuration result = config;
        mutex.unlock();
        return result;
    }

    public static void updateFromAPIResponse(APIResponse apiResponse) {
        mutex.lock();
        try {
            config.updateConfig(apiResponse);
        } catch (Throwable e) {
            logger.debug("An error occurred updating service config: %s", e.getMessage());
        }
        mutex.unlock();
    }

    public static void updateFromAPIListsResponse(Optional<ReportingApi.APIListsResponse> response) {
        mutex.lock();
        try {
            config.updateBlockedLists(response);
        } catch (Throwable e) {
            logger.debug("An error occurred updating service config: %s", e.getMessage());
        }
        mutex.unlock();
    }

    public static void updateServiceConfigBlocking(boolean blocking) {
        mutex.lock();
        config.setBlocking(blocking);
        mutex.unlock();
    }
}
