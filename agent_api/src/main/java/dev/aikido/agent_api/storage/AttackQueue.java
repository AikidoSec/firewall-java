package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Attack;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static dev.aikido.agent_api.background.cloud.api.events.DetectedAttack.createAPIEvent;

public final class AttackQueue {
    private static final Logger logger = LogManager.getLogger(AttackQueue.class);
    private static final BlockingQueue<APIEvent> queue = new LinkedBlockingQueue<>();

    private AttackQueue() {

    }

    public static void attackDetected(Attack attack, ContextObject context) {
        // increment statistics
        StatisticsStore.incrementAttacksDetected();
        if (ServiceConfigStore.getConfig().isBlockingEnabled()) {
            StatisticsStore.incrementAttacksBlocked(); // Also increment blocked attacks.
        }

        // generate attack API event
        APIEvent detectedAttack = createAPIEvent(attack, context);
        add(detectedAttack);
    }

    public static void add(APIEvent attack) {
        if (!queue.offer(attack)) {
            logger.debug("Failed to add attack to queue: %s", attack);
        }
    }

    public static APIEvent get() throws InterruptedException {
        return queue.take();
    }
}
