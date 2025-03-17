package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.StatisticsStore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import static dev.aikido.agent_api.storage.ConfigStore.getConfig;

public final class AttackQueueStore {
    private static final Logger logger = LogManager.getLogger(AttackQueueStore.class);
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final BlockingQueue<APIEvent> queue = new LinkedBlockingQueue<>();;
    private AttackQueueStore() {

    }

    public static void addAttackToQueue(Attack attack, ContextObject context) {
        // Generate an attack event :
        DetectedAttack.DetectedAttackEvent detectedAttack = DetectedAttack.createAPIEvent(attack, context);
        logger.debug("Detected %s Attack", attack.kind);

        // Increment statistics :
        StatisticsStore.incrementAttacksDetected();
        if (getConfig().isBlockingEnabled()) {
            StatisticsStore.incrementAttacksBlocked(); // Also increment blocked attacks.
        }

        mutex.lock();
        try {
            queue.add(detectedAttack); // Add to attack queue, so attack is reported in background
        } catch (Throwable e) {
            logger.debug("Error occurred adding an attack to the queue: %s", e.getMessage());
        } finally {
            mutex.unlock();
        }
    }
    public static APIEvent getAttackFromQueue() {
        mutex.lock();
        try {
            if(queue.isEmpty()) {
                return null;
            }
            return queue.poll();
        } finally {
            mutex.unlock();
        }
    }
}
