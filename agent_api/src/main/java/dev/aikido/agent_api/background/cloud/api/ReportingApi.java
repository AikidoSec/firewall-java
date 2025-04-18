package dev.aikido.agent_api.background.cloud.api;

import dev.aikido.agent_api.background.cloud.api.events.APIEvent;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public abstract class ReportingApi {
    public int timeoutInSec;

    public ReportingApi(int timeoutInSec) {
        this.timeoutInSec = timeoutInSec;
    }

    /**
     * Converts results into an API response object.
     *
     * @param res The response object containing status code and body.
     * @return A map representing the API response.
     */
    public abstract APIResponse toApiResponse(HttpResponse<String> res);

    /**
     * Report event to the Aikido server.
     *
     * @param event        The event to report.
     */
    public abstract Optional<APIResponse> report(APIEvent event);

    public record APIListsResponse(
            List<ListsResponseEntry> blockedIPAddresses,
            List<ListsResponseEntry> allowedIPAddresses,
            List<BotBlocklist> blockedUserAgents
    ) {}

    public record ListsResponseEntry(boolean monitor, String key, String source, String description, List<String> ips) {
    }

    public record BotBlocklist(boolean monitor, String key, String pattern) {
    }
    /**
     * Fetch blocked lists using a separate API call, these can include :
     * -> blocked IP Addresses (e.g. geo restrictions)
     * -> allowed IP Addresses (e.g. geo restrictions)
     * -> blocked User-Agents (e.g. bot blocking)
     */
    public abstract Optional<APIListsResponse> fetchBlockedLists();
}
