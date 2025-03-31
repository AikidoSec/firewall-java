package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ServiceConfigStore {
    private static final Logger logger = LogManager.getLogger(ServiceConfigStore.class);
    private static final ServiceConfiguration config = new ServiceConfiguration();
    private static final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private ServiceConfigStore() {
    }

    public static ServiceConfiguration getConfig() {
        mutex.readLock().lock();
        try {
            return config;
        } finally {
            mutex.readLock().unlock();
        }
    }

    public static void updateFromAPIResponse(APIResponse apiResponse) {
        mutex.writeLock().lock();
        try {
            logger.trace("Updating config from APIResponse");
            config.updateConfig(apiResponse);
        } catch (Throwable e) {
            logger.debug("An error occurred updating service config: %s", e.getMessage());
        } finally {
            mutex.writeLock().unlock();
        }
    }

    public static void updateFromAPIListsResponse(Optional<ReportingApi.APIListsResponse> response) {
        mutex.writeLock().lock();
        try {
            logger.trace("Updating config from APIListsResponse");
            config.updateBlockedLists(response);
        } catch (Throwable e) {
            logger.debug("An error occurred updating service config: %s", e.getMessage());
        } finally {
            mutex.writeLock().unlock();
        }
    }

    public static void updateBlocking(boolean blocking) {
        mutex.writeLock().lock();
        try {
            logger.trace("Blocking updated: %s", blocking);
            config.setBlocking(blocking);
        } finally {
            mutex.writeLock().unlock();
        }
    }

    public static void setMiddlewareInstalled(boolean middlewareInstalled) {
        // We want to avoid a write lock if possible for performance reasons :
        boolean currentMiddlewareInstalled = getConfig().isMiddlewareInstalled();
        if (currentMiddlewareInstalled == middlewareInstalled) {
            return;
        }

        mutex.writeLock().lock();
        try {
            logger.debug("middlewareInstalled updated: %s", middlewareInstalled);
            config.setMiddlewareInstalled(middlewareInstalled);
        } finally {
            mutex.writeLock().unlock();
        }
    }
}
