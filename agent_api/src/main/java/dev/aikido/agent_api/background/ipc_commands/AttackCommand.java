package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.Attack;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class AttackCommand extends Command<AttackCommand.Req, Command.EmptyResult> {
    public record Req(Attack attack, ContextObject context) {}

    private static final Logger logger = LogManager.getLogger(AttackCommand.class);
    private final BlockingQueue<APIEvent> queue;

    public AttackCommand(BlockingQueue<APIEvent> queue) {
        this.queue = queue;
    }
    public static void sendAttack(ThreadIPCClient client, Req req) {
        new AttackCommand(null).send(client, req);
    }

    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public String getName() { return "ATTACK"; }

    @Override
    public Class<Req> getInputClass() {
        return Req.class;
    }

    @Override
    public Class<EmptyResult> getOutputClass() {
        return EmptyResult.class;
    }

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

        // Increment statistics :
        connectionManager.getStats().incrementAttacksDetected();
        if (connectionManager.shouldBlock()) {
            connectionManager.getStats().incrementAttacksBlocked(); // Also increment blocked attacks.
        }

        queue.add(detectedAttack); // Add to attack queue, so attack is reported in background
        return Optional.empty();
    }
}
