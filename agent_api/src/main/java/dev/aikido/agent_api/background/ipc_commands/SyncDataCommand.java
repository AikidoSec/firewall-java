package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.routes.Routes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SyncDataCommand implements Command {
    public record SyncDataResult(List<Endpoint> endpoints, Set<String> blockedUserIDs, Set<String> bypassedIPs, Routes routes) {}
    @Override
    public boolean returnsData() {
        // Returns JSON of SyncDataResult
        return true;
    }

    @Override
    public boolean matchesName(String command) {
        return command.equalsIgnoreCase("SYNC_DATA");
    }

    /**
     *
     * @param data is an empty string/nothing
     * @return JSON Representation of {@code SyncDataResult}
     */
    @Override
    public Optional<String> execute(String data, CloudConnectionManager connectionManager) {
        List<Endpoint> endpoints = connectionManager.getConfig().getEndpoints();
        Set<String> blockedUserIDs = connectionManager.getConfig().getBlockedUserIDs();
        Set <String> bypassedIPs = connectionManager.getConfig().getBypassedIPs();
        Routes routes = connectionManager.getRoutes();
        SyncDataResult syncDataResult = new SyncDataResult(endpoints, blockedUserIDs, bypassedIPs, routes);

        Gson gson = new Gson();
        String syncDataResultJson = gson.toJson(syncDataResult);
        return Optional.of(syncDataResultJson);
    }
}
