package dev.aikido.AikidoAgent.background.ipc_commands;

public interface Command {
    public boolean returnsData();
    public boolean matchesName(String command);
    public void execute(String data);

}
