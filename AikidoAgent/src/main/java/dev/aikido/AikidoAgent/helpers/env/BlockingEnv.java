package dev.aikido.AikidoAgent.helpers.env;

public class BlockingEnv extends BooleanEnv {
    private static final String environmentName = "AIKIDO_BLOCKING";
    private static final boolean defaultValue = false;

    public BlockingEnv() {
        super(environmentName, defaultValue);
    }
}
