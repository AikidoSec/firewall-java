package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;

import java.util.Optional;

public class ServiceConfiguration {
    // template
    public boolean isMiddlewareInstalled() {
        return false;
    }

    public void setMiddlewareInstalled(boolean middlewareInstalled) {
    }

    public void setBlocking(boolean blocking) {
    }

    public void updateBlockedLists(Optional<ReportingApi.APIListsResponse> response) {
    }

    public void updateConfig(APIResponse apiResponse) {
    }
}
