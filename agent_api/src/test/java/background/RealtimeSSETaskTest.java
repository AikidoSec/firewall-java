package background;

import dev.aikido.agent_api.background.RealtimeSSETask;
import dev.aikido.agent_api.background.cloud.RealtimeSSEAPI;
import dev.aikido.agent_api.background.cloud.SSEParser;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.helpers.env.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class RealtimeSSETaskTest {
    private ReportingApiHTTP reportingApi;
    private RealtimeSSETask task;

    @BeforeEach
    public void setUp() {
        reportingApi = mock(ReportingApiHTTP.class);
        task = new RealtimeSSETask(new RealtimeSSEAPI(new Token("token")), reportingApi);
    }

    private APIResponse sampleConfig(long configUpdatedAt) {
        return new APIResponse(true, null, configUpdatedAt, List.of(), List.of(), List.of(), false, List.of(), true, true, List.of());
    }

    @Test
    public void testIgnoresEventsThatAreNotConfigUpdated() {
        task.onEvent(new SSEParser.Event("ping", ""));

        verify(reportingApi, never()).fetchNewConfig();
    }

    @Test
    public void testIgnoresInvalidJsonPayload() {
        task.onEvent(new SSEParser.Event("config-updated", "not json"));

        verify(reportingApi, never()).fetchNewConfig();
    }

    @Test
    public void testIgnoresPayloadMissingConfigUpdatedAt() {
        task.onEvent(new SSEParser.Event("config-updated", "{\"foo\":\"bar\"}"));

        verify(reportingApi, never()).fetchNewConfig();
    }

    @Test
    public void testFetchesAndAppliesNewConfigOnNewerEvent() {
        when(reportingApi.fetchNewConfig()).thenReturn(Optional.of(sampleConfig(200)));
        when(reportingApi.fetchBlockedLists()).thenReturn(Optional.empty());

        task.onEvent(new SSEParser.Event("config-updated", "{\"configUpdatedAt\":200}"));

        verify(reportingApi, times(1)).fetchNewConfig();
        verify(reportingApi, times(1)).fetchBlockedLists();
    }

    @Test
    public void testIgnoresEventThatIsNotNewer() {
        when(reportingApi.fetchNewConfig()).thenReturn(Optional.of(sampleConfig(200)));
        when(reportingApi.fetchBlockedLists()).thenReturn(Optional.empty());

        task.onEvent(new SSEParser.Event("config-updated", "{\"configUpdatedAt\":200}"));
        task.onEvent(new SSEParser.Event("config-updated", "{\"configUpdatedAt\":150}"));

        verify(reportingApi, times(1)).fetchNewConfig();
    }

    @Test
    public void testHandlesFetchFailureGracefully() {
        when(reportingApi.fetchNewConfig()).thenReturn(Optional.empty());
        when(reportingApi.fetchBlockedLists()).thenReturn(Optional.empty());

        task.onEvent(new SSEParser.Event("config-updated", "{\"configUpdatedAt\":200}"));

        verify(reportingApi, times(1)).fetchNewConfig();
    }
}
