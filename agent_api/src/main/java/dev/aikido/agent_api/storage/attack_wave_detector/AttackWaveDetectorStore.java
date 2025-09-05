package dev.aikido.agent_api.storage.attack_wave_detector;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.concurrent.locks.ReentrantLock;

public final class AttackWaveDetectorStore {
    private static final Logger logger = LogManager.getLogger(AttackWaveDetectorStore.class);
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final AttackWaveDetector detector = new AttackWaveDetector();

    private AttackWaveDetectorStore() {
    }

    public static boolean check(ContextObject ctx) {
        mutex.lock();
        try {
            return detector.check(ctx);
        } catch (Throwable e) {
            logger.debug("An error occurred checking for attack waves: %s", e.getMessage());
            return false;
        } finally {
            mutex.unlock();
        }
    }
}
