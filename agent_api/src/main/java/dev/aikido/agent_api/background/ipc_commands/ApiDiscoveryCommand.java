package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.routes.RouteEntry;

import java.io.Serializable;
import java.util.Optional;

public class ApiDiscoveryCommand extends Command<ApiDiscoveryCommand.Req, Command.EmptyResult> {
    public record Req(APISpec apiSpec, RouteMetadata routeMetadata) implements Serializable {}

    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public String getName() { return "API_DISCOVERY"; }

    @Override
    public Optional<EmptyResult> execute(Req request, CloudConnectionManager connectionManager) {
        if (request != null && request.routeMetadata() != null) {
            RouteEntry route = connectionManager.getRoutes().get(request.routeMetadata());
            if (route != null) {
                route.updateApiSpec(request.apiSpec());
            }
        }
        return Optional.empty();
    }
}
