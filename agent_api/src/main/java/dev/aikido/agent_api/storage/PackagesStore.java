package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class PackagesStore {
    private static final Logger logger = LogManager.getLogger(PackagesStore.class);
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Packages packages = new Packages();

    public void addPackage(String packageName, String packageVersion) {
        mutex.lock();
        try {
            packages.addPackage(packageName, packageVersion);
        } catch (Throwable t) {
            logger.debug("Failed to add package %s: %s", packageName, t);
        } finally {
            mutex.unlock();
        }
    }

    public List<Packages.PackageInfo> asArray() {
        mutex.lock();
        try {
            return packages.asArray();
        } catch (Throwable t) {
            logger.debug("Error while clearing packages %s", t);
            return null;
        } finally {
            mutex.unlock();
        }
    }

    public void clear() {
        mutex.lock();
        try {
            packages.clear();
        } catch (Throwable t) {
            logger.debug("Error while clearing packages %s", t);
        } finally {
            mutex.unlock();
        }
    }
}
