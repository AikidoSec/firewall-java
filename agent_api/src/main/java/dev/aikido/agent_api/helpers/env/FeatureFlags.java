package dev.aikido.agent_api.helpers.env;

public enum FeatureFlags {
    AIKIDO_FEATURE_SSE;

    public boolean isEnabled() {
        return new BooleanEnv(name(), false).getValue();
    }
}
