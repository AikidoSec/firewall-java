package background.cloud.api;


import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.background.cloud.api.events.Heartbeat;
import dev.aikido.agent_api.storage.ConfigStore;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.Statistics;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.context.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class HeartbeatEventTest {
    @Test
    public void testGetHeartbeatEvent() {
        // Arrange
        Statistics.StatsRecord stats = new Statistics.StatsRecord(0, 1, null);
        var hostnames = new Hostnames(5000);
        hostnames.add("aikido.dev", 8080);
        RouteEntry[] routes = new RouteEntry[0]; // Replace with actual RouteEntry array if needed
        List<User> users = Collections.emptyList(); // Replace with actual User list if needed

        ConfigStore.setMiddlewareInstalled(false);

        // Act
        Heartbeat.HeartbeatEvent event = Heartbeat.get(stats, hostnames.asArray(), routes, users);

        // Assert
        assertEquals("heartbeat", event.type());
        assertEquals(stats, event.stats());
        assertArrayEquals(hostnames.asArray(), event.hostnames());
        assertEquals(routes, event.routes());
        assertEquals(users, event.users());
        assertEquals(false, event.middlewareInstalled());

        // Test middleware installed as well :
        ConfigStore.setMiddlewareInstalled(true);
        Heartbeat.HeartbeatEvent event2 = Heartbeat.get(stats, hostnames.asArray(), routes, users);
        assertEquals(true, event2.middlewareInstalled());


    }
}