package dev.aikido.agent_api.background.cloud.api;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.storage.service_configuration.Domain;

import java.util.List;

public record APIResponse(
        boolean success,
        String error,
        long configUpdatedAt,
        List<Endpoint> endpoints,
        List<String> blockedUserIds,
        List<String> allowedIPAddresses,
        boolean blockNewOutgoingRequests,
        List<Domain> domains,
        boolean receivedAnyStats,
        boolean block
) {
}
