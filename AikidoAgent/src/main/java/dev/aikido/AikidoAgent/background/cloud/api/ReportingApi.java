package dev.aikido.AikidoAgent.background.cloud.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.aikido.AikidoAgent.background.cloud.api.events.APIEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class ReportingApi {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts results into an API response object.
     *
     * @param res The response object containing status code and body.
     * @return A map representing the API response.
     */
    /*
    public Map<String, Object> toApiResponse(ApiResponse res) {
        Map<String, Object> response = new HashMap<>();
        int status = res.getStatusCode();

        if (status == 429) {
            response.put("success", false);
            response.put("error", "rate_limited");
        } else if (status == 401) {
            response.put("success", false);
            response.put("error", "invalid_token");
        } else if (status == 200) {
            try {
                Map<String, Object> data = objectMapper.readValue(res.getBody(), Map.class);
                return data; // Return the parsed JSON data
            } catch (IOException e) {
                System.out.print("Error parsing response body: " + e.getMessage() + "\n");
                System.out.print("Response body: " + res.getBody() + "\n");
            }
        }
        response.put("success", false);
        response.put("error", "unknown_error");
        return response;
    }
    */

    /**
     * Report event to the Aikido server.
     *
     * @param token           The authentication token.
     * @param event           The event to report.
     * @param timeoutInSec    The timeout in seconds.
     */
    public abstract void report(String token, APIEvent event, int timeoutInSec);
}
