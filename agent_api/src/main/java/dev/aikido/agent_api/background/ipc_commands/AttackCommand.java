package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.background.utilities.ThreadClient;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.Attack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class AttackCommand extends Command<AttackCommand.Req, Command.EmptyResult> {
    public record Req(Attack attack, ContextObject context) implements Serializable {}

    private static final Logger logger = LogManager.getLogger(AttackCommand.class);
    private final BlockingQueue<APIEvent> queue;

    public AttackCommand(BlockingQueue<APIEvent> queue) {
        this.queue = queue;
    }
    public static void sendAttack(ThreadClient client, Req req) {
        logger.debug("Attack detected: {}", req);
        new AttackCommand(null).send(client, req);
    }

    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public String getName() { return "ATTACK"; }

    @Override
    public Optional<EmptyResult> execute(Req data, CloudConnectionManager connectionManager) {
        if (data.attack == null || data.context == null) {
            logger.debug("Attack or context not defined correctly, returning.");
            return Optional.empty();
        }
        // Generate an attack event :
        DetectedAttack.DetectedAttackEvent detectedAttack = DetectedAttack.createAPIEvent(
                data.attack, data.context, connectionManager
        );
        queue.add(detectedAttack); // Add to attack queue, so attack is reported in background
        return Optional.empty();
    }
}
