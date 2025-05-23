package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.concurrent.locks.ReentrantLock;

public final class HostnamesStore {
    private static final Logger logger = LogManager.getLogger(HostnamesStore.class);
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Hostnames hostnames = new Hostnames(5000); // max entry size is 5000

    private HostnamesStore() {

    }

    public static Hostnames.HostnameEntry[] getHostnamesAsList() {
        mutex.lock();
        try {
            return hostnames.asArray();
        } catch (Throwable e) {
            logger.debug("An error occurred getting the hostnames as a list: %s", e.getMessage());
            return null; // an error occurred, so we don't have hostnames list
        } finally {
            mutex.unlock();
        }
    }

    public static void incrementHits(String hostname, int port) {
        mutex.lock();
        try {
            hostnames.add(hostname, port);
        } catch (Throwable e) {
            logger.debug("An error occurred adding hits for hostname, error: %s", e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public static void clear() {
        mutex.lock();
        try {
            hostnames.clear();
        } finally {
            mutex.unlock();
        }
    }
}
