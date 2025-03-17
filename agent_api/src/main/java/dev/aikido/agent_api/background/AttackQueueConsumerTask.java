package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import static dev.aikido.agent_api.vulnerabilities.AttackQueueStore.getAttackFromQueue;

public class AttackQueueConsumerTask extends TimerTask {
    private final CloudConnectionManager connectionManager;
    public AttackQueueConsumerTask(CloudConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        APIEvent event = getAttackFromQueue();
        while (event != null) {
            this.connectionManager.reportEvent(event, false /* should not update config */);
            event = getAttackFromQueue(); // refresh
        }
    }
}
