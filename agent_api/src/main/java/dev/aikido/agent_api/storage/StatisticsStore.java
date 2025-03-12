package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.concurrent.locks.ReentrantLock;

public final class StatisticsStore {
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Statistics stats = new Statistics();

    private StatisticsStore() {

    }

    public static Statistics getStats() {
        mutex.lock();
        Statistics result = StatisticsStore.stats;
        mutex.unlock();
        return result;
    }

    public static void incrementHits() {
        mutex.lock();
        stats.incrementHits();
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
