package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.storage.AttackQueue;

import java.util.TimerTask;

public class AttackQueueConsumerTask extends TimerTask {
    private final ReportingApiHTTP api;
    public AttackQueueConsumerTask(ReportingApiHTTP api) {
        this.api = api;
    }

    @Override
    public void run() {
        // Check if queue contains events that need to be sent:
        APIEvent event;
        do {
            try {
                event = AttackQueue.get();
                this.api.report(event);
            } catch (InterruptedException ignored) {
                break;
            }
        } while (true);
    }
}
