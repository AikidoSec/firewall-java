package background.cloud.api;

import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.background.cloud.api.events.Heartbeat;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.Statistics;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.context.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HeartbeatEventTest {

    private GetManagerInfo.ManagerInfo managerInfo;
    private MockedStatic<GetManagerInfo> mockedGetManagerInfo;

    @BeforeEach
    public void setUp() {
        managerInfo = new GetManagerInfo.ManagerInfo(
                false, // dryMode
                "localhost", // hostname
                "1.0.0", // version
                "firewall-java", // library
                "127.0.0.1", // ipAddress
                Collections.emptyMap(), // packages
                "serverless", // serverless
                Collections.emptyList(), // stack
                new GetManagerInfo.OS("Linux", "5.4.0"), // os
                false, // preventedPrototypePollution
                "development", // nodeEnv
                new GetManagerInfo.Platform("Java", "11") // platform
        );

        mockedGetManagerInfo = mockStatic(GetManagerInfo.class);
    }

    @Test
    public void testGetHeartbeatEvent() {
        // Arrange
        Statistics.StatsRecord stats = new Statistics.StatsRecord(0, 1, null);
        var hostnames = new Hostnames(5000);
        hostnames.add("aikido.dev", 8080);
        RouteEntry[] routes = new RouteEntry[0]; // Replace with actual RouteEntry array if needed
        List<User> users = Collections.emptyList(); // Replace with actual User list if needed

        mockedGetManagerInfo.when(GetManagerInfo::getManagerInfo).thenReturn(managerInfo);
        ServiceConfigStore.setMiddlewareInstalled(false);

        // Act
        Heartbeat.HeartbeatEvent event = Heartbeat.get(stats, hostnames.asArray(), routes, users);

        // Assert
        assertEquals("heartbeat", event.type());
        assertEquals(managerInfo, event.agent());
        assertEquals(stats, event.stats());
        assertArrayEquals(hostnames.asArray(), event.hostnames());
        assertEquals(routes, event.routes());
        assertEquals(users, event.users());
        assertFalse(event.middlewareInstalled());

        // Test middleware installed as well :
        ServiceConfigStore.setMiddlewareInstalled(true);
        Heartbeat.HeartbeatEvent event2 = Heartbeat.get(stats, hostnames.asArray(), routes, users);
        assertTrue(event2.middlewareInstalled());
    }
}
