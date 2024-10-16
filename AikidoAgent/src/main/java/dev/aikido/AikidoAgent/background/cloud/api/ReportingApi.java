package dev.aikido.AikidoAgent.background.cloud.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.aikido.AikidoAgent.background.cloud.api.events.APIEvent;

import java.net.http.HttpResponse;

public abstract class ReportingApi {
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
     * @param token           The authentication token.
     * @param event           The event to report.
     * @param timeoutInSec    The timeout in seconds.
     */
    public abstract void report(String token, APIEvent event, int timeoutInSec);
}
