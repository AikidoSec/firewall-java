package dev.aikido.AikidoAgent.background.cloud.api;

import dev.aikido.AikidoAgent.background.Endpoint;

import java.util.List;

public record APIResponse(
        boolean success,
        String error,
        long configUpdatedAt,
        List<Endpoint> endpoints,
        List<String> blockedUserIds,
        List<String> allowedIPAddresses,
        boolean receivedAnyStats,
        boolean block
) {
}
