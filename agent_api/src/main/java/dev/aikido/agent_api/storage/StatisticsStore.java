package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.concurrent.locks.ReentrantLock;

public final class StatisticsStore {
    private static final Logger logger = LogManager.getLogger(StatisticsStore.class);
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Statistics stats = new Statistics();

    private StatisticsStore() {

    }

    public static Statistics.StatsRecord getStatsRecord() {
        Statistics.StatsRecord result = null;
        mutex.lock();
        try {
            result = stats.getRecord();
        } catch (Throwable e) {
            logger.debug("An error occurred getting the stats record: %s", e.getMessage());
        }
        mutex.unlock();
        return result;
    }

    public static void incrementHits() {
        mutex.lock();
        stats.incrementTotalHits(1);
        mutex.unlock();
    }

    public static void incrementAttacksDetected() {
        mutex.lock();
        stats.incrementAttacksDetected();
        mutex.unlock();
    }

    public static void incrementAttacksBlocked() {
        mutex.lock();
        stats.incrementAttacksBlocked();
        mutex.unlock();
    }

    public static void clear() {
        mutex.lock();
        stats.clear();
        mutex.unlock();
    }
}
