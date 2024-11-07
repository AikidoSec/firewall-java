package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.Attack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class AttackCommand implements Command {
    private static final Logger logger = LogManager.getLogger(AttackCommand.class);
    private final BlockingQueue<APIEvent> queue;
    public record AttackCommandData(Attack attack, ContextObject context) {}
    public AttackCommand(BlockingQueue<APIEvent> queue) {
        this.queue = queue;
    }
    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public boolean matchesName(String name) {
        return name.equalsIgnoreCase("ATTACK");
    }

    @Override
    public Optional<String> execute(String data, CloudConnectionManager connectionManager) {
        Gson gson = new Gson();
        AttackCommandData attackCommandData = gson.fromJson(data, AttackCommandData.class);
        if (attackCommandData.attack == null || attackCommandData.context == null) {
            logger.debug("Attack or context not defined correctly, returning.");
            return Optional.empty();
        }
        // Generate an attack event :
        DetectedAttack.DetectedAttackEvent detectedAttack = DetectedAttack.createAPIEvent(
                attackCommandData.attack, attackCommandData.context, connectionManager
        );
        queue.add(detectedAttack); // Add to attack queue, so attack is reported in background
        return Optional.empty();
    }
}
