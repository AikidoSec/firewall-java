package dev.aikido.agent_api.storage.statistics;

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
        mutex.lock();
        try {
            return stats.getRecord();
        } catch (Throwable e) {
            logger.debug("An error occurred getting the stats record: %s", e.getMessage());
            return null; // error occurred, so we don't have a stats record
        } finally {
            mutex.unlock();
        }
    }

    public static void incrementHits() {
        mutex.lock();
        try {
            stats.incrementTotalHits(1);
        } finally {
            mutex.unlock();
        }
    }

    public static void incrementAttacksDetected() {
        mutex.lock();
        try {
            stats.incrementAttacksDetected();
        } finally {
            mutex.unlock();
        }
    }

    public static void incrementAttacksBlocked() {
        mutex.lock();
        try {
            stats.incrementAttacksBlocked();
        } finally {
            mutex.unlock();
        }
    }

    public static void registerCall(String sink, OperationKind kind) {
        mutex.lock();
        try {
            stats.registerCall(sink, kind);
        } finally {
            mutex.unlock();
        }
    }

    public static void clear() {
        mutex.lock();
        try {
            stats.clear();
        } finally {
            mutex.unlock();
        }
    }
}
