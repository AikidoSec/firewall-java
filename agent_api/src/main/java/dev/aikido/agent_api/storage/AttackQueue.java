package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class AttackQueue {
    private static final Logger logger = LogManager.getLogger(AttackQueue.class);
    private static final BlockingQueue<APIEvent> queue = new LinkedBlockingQueue<>();

    private AttackQueue() {

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
