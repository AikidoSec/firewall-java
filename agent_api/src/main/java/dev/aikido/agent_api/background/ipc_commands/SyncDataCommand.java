package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.storage.routes.Routes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SyncDataCommand extends Command<Command.EmptyResult, SyncDataCommand.Res> {
    public record Res(List<Endpoint> endpoints, Set<String> blockedUserIDs, Set<String> bypassedIPs, Routes routes, ReportingApi.APIListsResponse blockedListsRes) {}
    @Override
    public boolean returnsData() {
        // Returns JSON of SyncDataResult
        return true;
    }

    @Override
    public String getName() { return "SYNC_DATA"; }

    @Override
    public Class<EmptyResult> getInputClass() {
        return EmptyResult.class;
    }

    @Override
    public Class<Res> getOutputClass() {
        return Res.class;
    }

    /**
     *
     * @param data is an empty string/nothing
     * @return {@code SyncDataResult}
     */
    @Override
    public Optional<Res> execute(EmptyResult data, CloudConnectionManager connectionManager) {
        List<Endpoint> endpoints = List.of();
        Set<String> blockedUserIDs = Set.of();
        Set <String> bypassedIPs = Set.of();

        Res syncDataResult = new Res(endpoints, blockedUserIDs, bypassedIPs, null, null);
        return Optional.of(syncDataResult);
    }
}
