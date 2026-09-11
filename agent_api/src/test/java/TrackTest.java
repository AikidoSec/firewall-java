import dev.aikido.agent_api.Track;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.CustomEvent;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.AttackQueue;
import org.junit.jupiter.api.*;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;
import utils.EmptySampleContextObject;

import static org.junit.jupiter.api.Assertions.*;

@SetEnvironmentVariable(key = "AIKIDO_LOG_LEVEL", value = "trace")
@SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
public class TrackTest {
    @BeforeEach
    public void setup() {
        Context.set(null);
        AttackQueue.clear();
        Track.reset();
    }

    @AfterEach
    public void tearDown() {
        Context.set(null);
        AttackQueue.clear();
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
        int occurrences = out.capturedString().split("track\\(\\.\\.\\.\\) was called without a context\\.", -1).length - 1;
        assertEquals(1, occurrences);
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
}
