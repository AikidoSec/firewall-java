package dev.aikido.AikidoAgent.background.cloud.api.events;

public sealed interface APIEvent permits DetectedAttack.DetectedAttackEvent, Started.StartedEvent {}
