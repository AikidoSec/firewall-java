package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.TimerTask;

import static dev.aikido.agent_api.vulnerabilities.AttackQueueStore.getAttackFromQueue;

public class AttackQueueConsumerTask extends TimerTask {
    private final CloudConnectionManager connectionManager;
    private final static Logger logger = LogManager.getLogger(AttackQueueConsumerTask.class);
    public AttackQueueConsumerTask(CloudConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        APIEvent event = getAttackFromQueue();
        while (event != null) {
            logger.trace("reading from queue...");
            this.connectionManager.reportEvent(event, false /* should not update config */);
            event = getAttackFromQueue(); // refresh
        }
    }
}
