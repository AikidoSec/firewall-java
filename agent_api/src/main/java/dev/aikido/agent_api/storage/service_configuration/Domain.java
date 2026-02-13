package dev.aikido.agent_api.storage.service_configuration;

public record Domain(String hostname, String mode) {
    public boolean isBlockingMode() {
        // mode can either be "allow" or "block"
        return this.mode.equals("block");
    }
}
