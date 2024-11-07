package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.Heartbeat;
import dev.aikido.agent_api.background.routes.RouteEntry;
import dev.aikido.agent_api.context.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

public class AttackQueueConsumerTask extends TimerTask {
    private final CloudConnectionManager connectionManager;
    private final BlockingQueue<APIEvent> queue;
    public AttackQueueConsumerTask(CloudConnectionManager connectionManager, BlockingQueue<APIEvent> queue) {
        this.connectionManager = connectionManager;
        this.queue = queue;
    }

    @Override
    public void run() {
        // Check if queue contains events that need to be sent:
        while (!queue.isEmpty()) {
            try {
                APIEvent event = queue.take();
                this.connectionManager.reportEvent(event, false /* Should not update config */);
            } catch (InterruptedException ignored) {}
        }
    }
}
