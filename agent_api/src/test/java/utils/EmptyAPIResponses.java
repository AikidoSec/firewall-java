package utils;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.helpers.UnixTimeMS;

import java.util.List;

public class EmptyAPIResponses {
    public final static APIResponse emptyAPIResponse = new APIResponse(
            true, "", UnixTimeMS.getUnixTimeMS(), List.of(), List.of(), List.of(), true, false
    );
}
