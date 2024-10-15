package dev.aikido.AikidoAgent.background.cloud.api;

import java.util.List;

public record APIResponse(
        boolean success,
        String error,
        long configUpdatedAt,
        List<Object> endpoints,
        List<String> blockedUserIds,
        List<String> allowedIPAddresses,
        boolean receivedAnyStats
) {
}
