package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;

import java.util.Optional;

public class InitRouteCommand extends Command<RouteMetadata, Command.EmptyResult> {
    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public String getName() {
        return "INIT_ROUTE";
    }

    @Override
    public Class<RouteMetadata> getInputClass() {
        return RouteMetadata.class;
    }

    @Override
    public Class<EmptyResult> getOutputClass() {
        return EmptyResult.class;
    }

    /**
     *
     * @param routeMetadata of class RouteMetadata
     * @return Nothing (Empty optional)
     */
    @Override
    public Optional<EmptyResult> execute(RouteMetadata routeMetadata, CloudConnectionManager connectionManager) {
        if (connectionManager.getRoutes().get(routeMetadata) == null) {
            connectionManager.getRoutes().initializeRoute(routeMetadata);
        }
        connectionManager.getRoutes().get(routeMetadata).incrementHits();
        return Optional.empty();
    }
}