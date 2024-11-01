package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.User;

import java.util.Optional;

public class RegisterUserCommand implements Command {
    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public boolean matchesName(String command) {
        return command.equalsIgnoreCase("REGISTER_USER");
    }

    @Override
    public Optional<String> execute(String data, CloudConnectionManager connectionManager) {
        User user = new Gson().fromJson(data, User.class);
        if (user != null) {
            // Register user in connection manager:
            connectionManager.getUsers().addUser(user);
        }
        return Optional.empty();
    }
}
