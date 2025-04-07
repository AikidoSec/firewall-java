package dev.aikido.agent_api.storage.statistics;

public record OperationRecord(
        OperationKind kind,
        long total
) {
}
