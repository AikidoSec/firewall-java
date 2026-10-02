package background.cloud.api;

import dev.aikido.agent_api.background.cloud.api.events.CustomEvent;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import static org.junit.jupiter.api.Assertions.*;

class CustomEventTest {

    @Test
    void createAPIEvent_WithValidContext_ReturnsCustomEventEvent() {
        ContextObject context = new EmptySampleContextObject("test", "/api/resource", "POST");
        context.setUser(new User("user-1", "Jane Doe", "192.168.1.1", 1000L));

        CustomEvent.CustomEventEvent event = CustomEvent.createAPIEvent("my-custom-event", context);

        assertNotNull(event);
        assertEquals("custom", event.type());
        assertEquals("my-custom-event", event.name());
        assertNotNull(event.request());
        assertEquals("POST", event.request().method());
        assertEquals("web", event.request().source());
        assertEquals("/api/resource", event.request().route());
        assertEquals("192.168.1.1", event.request().ipAddress());
        assertNotNull(event.agent());
        assertNotNull(event.user());
        assertEquals("user-1", event.user().id());
        assertEquals("Jane Doe", event.user().name());
        assertTrue(event.time() > 0);
    }

    @Test
    void createAPIEvent_WithValidContext_UserOnlyContainsIdAndName() {
        ContextObject context = new EmptySampleContextObject("test", "/api/resource", "POST");
        context.setUser(new User("user-1", "Jane Doe", "192.168.1.1", 1000L));

        CustomEvent.CustomEventEvent event = CustomEvent.createAPIEvent("my-custom-event", context);

        assertEquals(2, CustomEvent.UserData.class.getRecordComponents().length);
    }

    @Test
    void createAPIEvent_WithNullContext_ReturnsCustomEventEventWithNullRequestAndUser() {
        CustomEvent.CustomEventEvent event = CustomEvent.createAPIEvent("my-custom-event", null);

        assertNotNull(event);
        assertEquals("custom", event.type());
        assertEquals("my-custom-event", event.name());
        assertNull(event.request());
        assertNull(event.user());
        assertNotNull(event.agent());
        assertTrue(event.time() > 0);
    }

    @Test
    void createAPIEvent_WithContextButNoUser_ReturnsCustomEventEventWithNullUser() {
        ContextObject context = new EmptySampleContextObject("test", "/api/resource", "GET");

        CustomEvent.CustomEventEvent event = CustomEvent.createAPIEvent("my-custom-event", context);

        assertNull(event.user());
    }
}
