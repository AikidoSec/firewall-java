package dev.aikido.AikidoAgent.background.ipc_commands;

public class AttackCommand implements Command {
    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public boolean matchesName(String name) {
        return name.equalsIgnoreCase("ATTACK");
    }

    @Override
    public void execute(String data) {

    }
}
