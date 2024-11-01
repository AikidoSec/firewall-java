package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;

import java.util.Optional;

public class InitRouteCommand implements Command {
    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public boolean matchesName(String command) {
        return command.equalsIgnoreCase("INIT_ROUTE");
    }

    /**
     *
     * @param data is JSON String for {@code RouteMetadata}
     * @return Nothing (Empty optional)
     */
    @Override
    public Optional<String> execute(String data, CloudConnectionManager connectionManager) {
        Gson gson = new Gson();
        RouteMetadata routeMetadata = gson.fromJson(data, RouteMetadata.class);
        connectionManager.getRoutes().initializeRoute(routeMetadata);
        return Optional.empty();
    }
}
