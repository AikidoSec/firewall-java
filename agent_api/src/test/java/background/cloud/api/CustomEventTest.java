package background.cloud.api;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.aikido.agent_api.background.cloud.api.events.CustomEvent;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import java.util.Set;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

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

        JsonObject serializedUser =
                new Gson().toJsonTree(event).getAsJsonObject().getAsJsonObject("user");
        assertEquals(Set.of("id", "name"), serializedUser.keySet());
        assertEquals("user-1", serializedUser.get("id").getAsString());
        assertEquals("Jane Doe", serializedUser.get("name").getAsString());
    }

    @Test
    void createAPIEvent_WithContextButNoUser_ReturnsCustomEventEventWithNullUser() {
        ContextObject context = new EmptySampleContextObject("test", "/api/resource", "GET");

        CustomEvent.CustomEventEvent event = CustomEvent.createAPIEvent("my-custom-event", context);

        assertNull(event.user());
    }
}
