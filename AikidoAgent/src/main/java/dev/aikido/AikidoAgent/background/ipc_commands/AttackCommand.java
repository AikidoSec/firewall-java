package dev.aikido.AikidoAgent.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.api.events.APIEvent;
import dev.aikido.AikidoAgent.background.cloud.api.events.DetectedAttack;
import dev.aikido.AikidoAgent.context.ContextObject;
import dev.aikido.AikidoAgent.vulnerabilities.Attack;

public class AttackCommand implements Command {
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
    public void execute(String data, CloudConnectionManager connectionManager) {
        Gson gson = new Gson();
        AttackCommandData attackCommandData = gson.fromJson(data, AttackCommandData.class);
        if (attackCommandData.attack == null || attackCommandData.context == null) {
            System.out.println("Attack or context not defined correctly, returning.");
            return;
        }
        // Generate an attack event :
        DetectedAttack.DetectedAttackEvent detectedAttack = DetectedAttack.createAPIEvent(
                attackCommandData.attack, attackCommandData.context, connectionManager
        );
        // Send to cloud :
        connectionManager.onDetectedAttack(detectedAttack);
    }
}
