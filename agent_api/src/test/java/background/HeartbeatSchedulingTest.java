package dev.aikido.agent_api.background;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.ServiceConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HeartbeatSchedulingTest {
    private ReportingApiHTTP api;
    private HeartbeatTask task;
    private ScheduledExecutorService scheduler;

    private static APIResponse config(String fields) {
        return new Gson().fromJson("{\"success\":true," + fields + "}", APIResponse.class);
    }

    @BeforeEach
    void setUp() {
        resetInterval();
        api = mock(ReportingApiHTTP.class);
        when(api.report(any())).thenReturn(Optional.empty());
        task = new HeartbeatTask(api);
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

    @Test
    void keepsDefaultIntervalWhenSettingIsAbsent() {
        assertEquals(600_000, new ServiceConfiguration().getHeartbeatIntervalInMS());
        ServiceConfigStore.updateFromAPIResponse(config("\"receivedAnyStats\":true"));
        scheduler.schedule(BackgroundProcess.createRecurringHeartbeat(task, scheduler),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        verifyNoInteractions(api);
        scheduledTask(600_000).run();
        scheduledTask(600_000).run();
        verify(api, times(2)).report(any());
    }

    @Test
    void appliesIntervalWhenSchedulingNextHeartbeat() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, null, 0, null, null, null, false, null, false, false, null, 120_000));
        scheduler.schedule(BackgroundProcess.createRecurringHeartbeat(task, scheduler),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        verifyNoInteractions(api);
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":300000"));
        scheduledTask(120_000).run();
        scheduledTask(300_000).run();
        verify(api, times(2)).report(any());
    }

    @Test
    void appliesIntervalFromHeartbeatResponse() {
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":120000"));
        when(api.report(any())).thenReturn(Optional.of(config("\"heartbeatIntervalInMS\":300000")));
        scheduler.schedule(BackgroundProcess.createRecurringHeartbeat(task, scheduler),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        scheduledTask(120_000).run();
        verify(api).report(any());
        scheduledTask(300_000).run();
        verify(api, times(2)).report(any());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, 60_000, 119_999})
    void ignoresIntervalsBelowMinimum(long interval) {
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":" + interval));
        assertEquals(600_000, ServiceConfigStore.getConfig().getHeartbeatIntervalInMS());
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":120000"));
        ServiceConfigStore.updateFromAPIResponse(config("\"heartbeatIntervalInMS\":" + interval));
        scheduler.schedule(BackgroundProcess.createRecurringHeartbeat(task, scheduler),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        scheduledTask(120_000).run();
        verify(api).report(any());
    }

    @Test
    void ignoresFailedConfigurationResponse() {
        ServiceConfigStore.updateFromAPIResponse(new Gson().fromJson(
                "{\"success\":false,\"heartbeatIntervalInMS\":120000}", APIResponse.class));
        scheduler.schedule(BackgroundProcess.createRecurringHeartbeat(task, scheduler),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        scheduledTask(600_000);
        verifyNoInteractions(api);
        assertEquals(600_000, ServiceConfigStore.getConfig().getHeartbeatIntervalInMS());
    }

    @Test
    void preservesInitialStatsHeartbeat() {
        HeartbeatTask initial = new HeartbeatTask(api, true);
        initial.run();
        verifyNoInteractions(api);
        ServiceConfigStore.updateFromAPIResponse(config(
                "\"heartbeatIntervalInMS\":120000,\"receivedAnyStats\":false"));
        initial.run();
        verify(api).report(any());
        scheduler.schedule(BackgroundProcess.createRecurringHeartbeat(task, scheduler),
                ServiceConfigStore.getConfig().getHeartbeatIntervalInMS(), TimeUnit.MILLISECONDS);
        scheduledTask(120_000).run();
        verify(api, times(2)).report(any());
    }
}
