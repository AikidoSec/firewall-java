package dev.aikido.agent_api.helpers.env;

public class BlockingEnv extends BooleanEnv {
    private static final String environmentName = "AIKIDO_BLOCKING";
    private static final boolean defaultValue = false;

    public BlockingEnv() {
        super(environmentName, defaultValue);
    }
}
