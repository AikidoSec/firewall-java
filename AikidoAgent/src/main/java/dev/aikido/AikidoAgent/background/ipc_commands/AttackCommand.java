package dev.aikido.AikidoAgent.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.api.events.DetectedAttack;
import dev.aikido.AikidoAgent.context.ContextObject;
import dev.aikido.AikidoAgent.vulnerabilities.Attack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class AttackCommand implements Command {
    private static final Logger logger = LogManager.getLogger(AttackCommand.class);
    private record AttackCommandData(Attack attack, ContextObject context) {}

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
        // Send to cloud :
        connectionManager.onDetectedAttack(detectedAttack);
        return Optional.empty();
    }
}
