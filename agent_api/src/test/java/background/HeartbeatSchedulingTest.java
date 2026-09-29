package dev.aikido.agent_api.background;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.ServiceConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HeartbeatSchedulingTest {
    private ReportingApiHTTP api;
    private ScheduledExecutorService scheduler;

    private static APIResponse config(String fields) {
        return new Gson().fromJson("{\"success\":true," + fields + "}", APIResponse.class);
    }

    @BeforeEach
    void setUp() {
        resetInterval();
        api = mock(ReportingApiHTTP.class);
        when(api.report(any())).thenReturn(Optional.empty());
        scheduler = mock(ScheduledExecutorService.class);
    }

    @AfterEach
    void resetInterval() {
        ServiceConfigStore.updateFromAPIResponse(config(
                "\"heartbeatIntervalInMS\":600000,\"receivedAnyStats\":true"));
    }

    private Runnable scheduledTask(long delay) {
        ArgumentCaptor<Runnable> callback = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(callback.capture(), eq(delay), eq(TimeUnit.MILLISECONDS));
        clearInvocations(scheduler);
        return callback.getValue();
    }

    @ParameterizedTest
    @CsvSource({"60000, false", "120000, false", "600000, false", "60000, true", "120000, true", "600000, true"})
    void sendsTwoEarlyHeartbeatsThenUsesConfiguredInterval(long interval, boolean receivedAnyStats) {
        try (var executors = mockStatic(Executors.class);
             var clients = mockConstruction(ReportingApiHTTP.class, (client, context) ->
                     when(client.report(any())).thenReturn(Optional.of(config(
                             "\"heartbeatIntervalInMS\":" + interval + ",\"enabledFeatures\":[],\"receivedAnyStats\":" + receivedAnyStats))))) {
            executors.when(() -> Executors.newScheduledThreadPool(3)).thenReturn(scheduler);

            new BackgroundProcess("heartbeat-test", new Token("token")).run();

            ArgumentCaptor<Runnable> initial = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).schedule(initial.capture(), eq(30L), eq(TimeUnit.SECONDS));
            verify(scheduler).scheduleAtFixedRate(any(RealtimeTask.class), eq(60L), eq(60L), eq(TimeUnit.SECONDS));
            verify(scheduler).scheduleAtFixedRate(any(AttackQueueConsumerTask.class), eq(0L), eq(2L), eq(TimeUnit.SECONDS));
            verifyNoMoreInteractions(scheduler);
            clearInvocations(scheduler);

            ReportingApiHTTP client = clients.constructed().get(0);
            verify(client).report(any());
            initial.getValue().run();
            verify(client, times(2)).report(any());
            scheduledTask(120_000).run();
            verify(client, times(3)).report(any());
            scheduledTask(interval).run();
            verify(client, times(4)).report(any());
            scheduledTask(interval);
        }
    }

    @Test
    void keepsSecondHeartbeatDelayWhenFirstResponseUpdatesInterval() {
        when(api.report(any())).thenReturn(Optional.of(config("\"heartbeatIntervalInMS\":300000")));
        BackgroundProcess.createHeartbeatTask(api, scheduler, true).run();
        scheduledTask(120_000).run();
        verify(api, times(2)).report(any());
        scheduledTask(300_000);
    }

    @Test
    void continuesStartupScheduleAfterFailedReport() {
        BackgroundProcess.createHeartbeatTask(api, scheduler, true).run();
        scheduledTask(120_000).run();
        verify(api, times(2)).report(any());
        scheduledTask(600_000);
    }

    @Test
    void keepsDefaultIntervalWhenSettingIsAbsent() {
        assertEquals(600_000, new ServiceConfiguration().getHeartbeatIntervalInMS());
        ServiceConfigStore.updateFromAPIResponse(config("\"receivedAnyStats\":true"));
        scheduler.schedule(BackgroundProcess.createHeartbeatTask(api, scheduler, false),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        verifyNoInteractions(api);
        scheduledTask(600_000).run();
        scheduledTask(600_000).run();
        verify(api, times(2)).report(any());
    }

    @Test
    void appliesIntervalWhenSchedulingNextHeartbeat() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, null, 0, null, null, null, false, null, false, false, null, 60_000));
        scheduler.schedule(BackgroundProcess.createHeartbeatTask(api, scheduler, false),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        verifyNoInteractions(api);
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":300000"));
        scheduledTask(60_000).run();
        scheduledTask(300_000).run();
        verify(api, times(2)).report(any());
    }

    @Test
    void appliesIntervalFromHeartbeatResponse() {
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":120000"));
        when(api.report(any())).thenReturn(Optional.of(config("\"heartbeatIntervalInMS\":60000")));
        scheduler.schedule(BackgroundProcess.createHeartbeatTask(api, scheduler, false),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        scheduledTask(120_000).run();
        verify(api).report(any());
        scheduledTask(60_000).run();
        verify(api, times(2)).report(any());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, 30_000, 59_999})
    void ignoresIntervalsBelowMinimum(long interval) {
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":" + interval));
        assertEquals(600_000, ServiceConfigStore.getConfig().getHeartbeatIntervalInMS());
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":120000"));
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":" + interval));
        scheduler.schedule(BackgroundProcess.createHeartbeatTask(api, scheduler, false),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        scheduledTask(120_000).run();
        verify(api).report(any());
    }

    @Test
    void ignoresFailedConfigurationResponse() {
        ServiceConfigStore.updateFromAPIResponse(new Gson().fromJson(
                "{\"success\":false,\"heartbeatIntervalInMS\":120000}", APIResponse.class));
        scheduler.schedule(BackgroundProcess.createHeartbeatTask(api, scheduler, false),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        scheduledTask(600_000);
        verifyNoInteractions(api);
        assertEquals(600_000, ServiceConfigStore.getConfig().getHeartbeatIntervalInMS());
    }

}
