package dev.aikido.AikidoAgent.background;

import dev.aikido.AikidoAgent.background.cloud.api.APIResponse;

/**
 * This class holds all config objects from Aikido's servers, i.e. endpoints, blocked IPs, bypassed users, ...
 * It is essential for e.g. rate limiting
 */
public class ServiceConfiguration {
    private final String serverless;
    private boolean blockingEnabled;
    public ServiceConfiguration(boolean blockingEnabled, String serverless) {
        if (serverless != null && serverless.isEmpty()) {
            throw new IllegalArgumentException("Serverless cannot be an empty string");
        }
        this.blockingEnabled = blockingEnabled;
        this.serverless = serverless;
    }
    public void updateConfig(APIResponse apiResponse) {
        if (apiResponse == null || !apiResponse.success()) {
            return;
        }
        this.blockingEnabled = apiResponse.block();
    }

    public String getServerless() {
        return serverless;
    }

    public boolean isBlockingEnabled() {
        return blockingEnabled;
    }
}
