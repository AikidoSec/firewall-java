package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.User;

import java.util.Optional;

public class RegisterUserCommand extends Command<User, Command.EmptyResult> {
    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public String getName() { return "REGISTER_USER"; }

    @Override
    public Optional<EmptyResult> execute(User user, CloudConnectionManager connectionManager) {
        if (user != null) {
            // Register user in connection manager:
            connectionManager.getUsers().addUser(user);
        }
        return Optional.empty();
    }
}
