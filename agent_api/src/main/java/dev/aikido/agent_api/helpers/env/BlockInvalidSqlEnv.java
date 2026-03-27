package dev.aikido.agent_api.helpers.env;

public class BlockInvalidSqlEnv extends BooleanEnv {
    private static final String environmentName = "AIKIDO_BLOCK_INVALID_SQL";
    private static final boolean defaultValue = true;

    public BlockInvalidSqlEnv() {
        super(environmentName, defaultValue);
    }
}
