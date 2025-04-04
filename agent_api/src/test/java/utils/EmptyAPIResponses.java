package utils;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.UnixTimeMS;
import dev.aikido.agent_api.storage.ServiceConfigStore;

import java.util.List;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class EmptyAPIResponses {
    public final static APIResponse emptyAPIResponse = new APIResponse(
            true, "", UnixTimeMS.getUnixTimeMS(), List.of(), List.of(), List.of(), true, false
    );
    public final static ReportingApi.APIListsResponse emptyAPIListsResponse = new ReportingApi.APIListsResponse(
            List.of(), List.of(), ""
    );
    public static void setEmptyConfigWithEndpointList(List<Endpoint> endpoints) {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, "", getUnixTimeMS(), endpoints, List.of(), List.of(), true, false
        ));
    }
}
