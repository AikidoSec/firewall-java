package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;

import java.util.Optional;

public class ApiDiscoveryCommand implements Command {
    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public boolean matchesName(String command) {
        return command.equalsIgnoreCase("API_DISCOVERY");
    }

    public record Req(APISpec apiSpec, RouteMetadata routeMetadata) {}
    @Override
    public Optional<String> execute(String data, CloudConnectionManager connectionManager) {
        Gson gson = new Gson();
        Req request = gson.fromJson(data, Req.class);
        if (request != null) {
            connectionManager.getRoutes().get(request.routeMetadata())
                    .updateApiSpec(request.apiSpec());
        }
        return Optional.empty();
    }
}
