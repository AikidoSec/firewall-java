package dev.aikido.AikidoAgent.background.cloud.api.events;

public sealed interface APIEvent permits Attack.AttackEvent, Started.StartedEvent {}
