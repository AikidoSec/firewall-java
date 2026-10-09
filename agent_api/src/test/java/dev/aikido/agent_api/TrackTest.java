package dev.aikido.agent_api;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static org.junit.jupiter.api.Assertions.*;
import static utils.EmptyAPIResponses.emptyAPIResponse;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.CustomEvent;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.SpringWebfluxContextObject;
import dev.aikido.agent_api.storage.AttackQueue;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;
import utils.EmptySampleContextObject;

@SetEnvironmentVariable(key = "AIKIDO_LOG_LEVEL", value = "trace")
@SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
public class TrackTest {
    @BeforeEach
    @AfterEach
    public void resetState() {
        Context.reset();
        AttackQueue.clear();
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
        Track.reset();
    }

    @Test
    @StdIo
    public void testTrackWithInvalidEventName(StdOut out) throws Exception {
        Track.track("");
        assertTrue(out.capturedString().contains("expects a non-empty string as event name."));
        assertEquals(0, AttackQueue.getSize());
    }

    @Test
    @StdIo
    public void testTrackWithNullEventName(StdOut out) throws Exception {
        Track.track(null);
        assertTrue(out.capturedString().contains("expects a non-empty string as event name."));
        assertEquals(0, AttackQueue.getSize());
    }

    @Test
    @StdIo
    public void testTrackWithoutContext(StdOut out) throws Exception {
        Track.track("my-event");
        assertTrue(out.capturedString().contains("track(...) was called without a context."));
        assertEquals(0, AttackQueue.getSize());
    }

    @Test
    @StdIo
    public void testTrackWithoutContextOnlyLogsOnce(StdOut out) throws Exception {
        Track.track("my-event");
        Track.track("my-event");
        int occurrences =
                out.capturedString().split("track\\(\\.\\.\\.\\) was called without a context\\.", -1).length - 1;
        assertEquals(1, occurrences);
    }

    @Test
    @StdIo
    public void testTrackForBypassedIpDoesNotQueueOrWarn(StdOut out) {
        List<String> bypassedIps = List.of("192.168.1.1");
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
                true, "", getUnixTimeMS(), List.of(), List.of(), bypassedIps, false, null, true, false, List.of()));
        WebRequestCollector.report(new EmptySampleContextObject("test", "/track-me", "POST"));

        Track.track("my-custom-event");

        assertEquals(0, AttackQueue.getSize());
        assertFalse(out.capturedString().contains("track(...) was called without a context."));
    }

    @Test
    public void testTrackDoesNotQueueEventForWebfluxRequest() {
        Context.set(new SpringWebfluxContextObject(
                "POST", "http://localhost/login", new InetSocketAddress("1.2.3.4", 443),
                new HashMap<>(), Map.of(), Map.of()));

        Track.track("my-custom-event");

        assertEquals(0, AttackQueue.getSize());
    }

    @Test
    @ClearEnvironmentVariable(key = "AIKIDO_TOKEN")
    public void testTrackWithoutTokenDoesNotQueueEvent() {
        ContextObject context = new EmptySampleContextObject("test", "/track-me", "POST");
        Context.set(context);

        Track.track("my-custom-event");

        assertEquals(0, AttackQueue.getSize());
    }

    @Test
    public void testTrackSendsEventToQueue() throws InterruptedException {
        ContextObject context = new EmptySampleContextObject("test", "/track-me", "POST");
        Context.set(context);

        Track.track("my-custom-event");

        assertEquals(1, AttackQueue.getSize());
        APIEvent event = AttackQueue.get();
        assertInstanceOf(CustomEvent.CustomEventEvent.class, event);
        CustomEvent.CustomEventEvent customEvent = (CustomEvent.CustomEventEvent) event;
        assertEquals("custom", customEvent.type());
        assertEquals("my-custom-event", customEvent.name());
        assertEquals("POST", customEvent.request().method());
        assertEquals("/track-me", customEvent.request().route());
    }

    @Test
    public void testTrackAllowsUpToLimitPerRequest() {
        ContextObject context = new EmptySampleContextObject("test", "/track-me", "POST");
        Context.set(context);

        for (int i = 0; i < 25; i++) {
            Track.track("event-" + i);
        }

        assertEquals(25, AttackQueue.getSize());
    }

    @Test
    @StdIo
    public void testTrackDropsEventsOverLimitAndLogsWarningOnlyOnce(StdOut out) {
        ContextObject context = new EmptySampleContextObject("test", "/track-me", "POST");
        Context.set(context);

        for (int i = 0; i < 30; i++) {
            Track.track("event-" + i);
        }

        assertEquals(25, AttackQueue.getSize());
        long warningCount = out.capturedString()
                .lines()
                .filter(line -> line.contains("Only the first 25 events were tracked."))
                .count();
        assertEquals(1, warningCount);
    }
}
