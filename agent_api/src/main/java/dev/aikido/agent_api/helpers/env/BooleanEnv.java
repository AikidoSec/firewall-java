package dev.aikido.agent_api.helpers.env;

public class BooleanEnv {
    private final boolean value;
    public BooleanEnv(String environmentName, boolean defaultValue) {
        String env = System.getenv(environmentName);
        if(env != null && !env.isEmpty()) {
            this.value = (env.equals("1") || env.equalsIgnoreCase("true"));
            return;
        }
        this.value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }
}
