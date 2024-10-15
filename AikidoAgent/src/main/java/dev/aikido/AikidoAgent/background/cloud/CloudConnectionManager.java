package dev.aikido.AikidoAgent.background.cloud;

public class CloudConnectionManager {
    private boolean blockingEnabled;
    private String serverless;
    public CloudConnectionManager(boolean block, String serverless) {
        if (serverless != null && serverless.isEmpty()) {
            throw new IllegalArgumentException("Serverless cannot be an empty string");
        }
        this.blockingEnabled = block;
        this.serverless = serverless;
    }
    public boolean shouldBlock() {
        return this.blockingEnabled;
    }

    public String getServerless() {
        return serverless;
    }
    public GetManagerInfo.ManagerInfo getManagerInfo() {
        return GetManagerInfo.getManagerInfo(this);
    }
}
