package dev.aikido.agent_api.helpers.env;

public final class InstanceName {
    private InstanceName() {}

    public static String fromEnv() {
        String name = System.getenv("AIKIDO_INSTANCE_NAME");
        if (name == null || name.isEmpty()) {
            return null;
        }
        return name;
    }
}
