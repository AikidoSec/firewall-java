package dev.aikido.agent_api.helpers;


public final class BackgroundProcessIdentifier {
    private BackgroundProcessIdentifier() {}
    public static boolean isBackgroundProcess() {
        return Thread.currentThread().getClass().toString()
                .equals("class dev.aikido.agent_api.background.BackgroundProcess");
    }
}
