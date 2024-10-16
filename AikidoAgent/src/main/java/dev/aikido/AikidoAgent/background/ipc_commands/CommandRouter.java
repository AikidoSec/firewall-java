package dev.aikido.AikidoAgent.background.ipc_commands;

/**
 * Routes the string command input to the correct class
 */
public class CommandRouter {
    public CommandRouter() {
        // Do some funky business
    }

    /**
     * Parses input string as an IPC command.
     * @param input raw IPC command.
     * -> E.g. input = "ATTACK${'this': 'that'}"
     */
    public void parseIPCInput(String input) {
        // P
        int indexOfCommandSeparator = input.indexOf('$');
        if (indexOfCommandSeparator == -1) {
            System.out.println("Error : Separator not found.");
            return;
        }
        String command = input.substring(0, indexOfCommandSeparator);
        String data = input.substring(indexOfCommandSeparator + 1);
        System.out.println("Command: " + command + "; Data: " + data);
    }
}
