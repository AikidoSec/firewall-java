package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.concurrent.locks.ReentrantLock;

public final class ServiceConfigStore {
    private ServiceConfigStore() {}
    private static final Logger logger = LogManager.getLogger(ServiceConfigStore.class);
    private static final ServiceConfiguration serviceConfig = new ServiceConfiguration(false, null);
    private static final ReentrantLock mutex = new ReentrantLock();

    public static ServiceConfiguration getServiceConfig() {
        mutex.lock();
        ServiceConfiguration result = serviceConfig;
        mutex.unlock();
        return result;
    }
    public static void updateFromAPIResponse(APIResponse apiResponse) {
        mutex.lock();
        try {
            serviceConfig.updateConfig(apiResponse);
        } catch (Throwable e) {
            logger.debug("An error occurred updating service config: %s", e.getMessage());
        }
        mutex.unlock();
    }
    public static void updateServiceConfigBlocking(boolean blocking) {
        mutex.lock();
        serviceConfig.setBlocking(blocking);
        mutex.unlock();
    }
}
