package background.cloud.api;


import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.background.cloud.api.events.Heartbeat;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.context.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class HeartbeatEventTest {

    private CloudConnectionManager connectionManager;
    private GetManagerInfo.ManagerInfo managerInfo;

    @BeforeEach
    public void setUp() {
        connectionManager = Mockito.mock(CloudConnectionManager.class);
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
    }

    @Test
    public void testGetHeartbeatEvent() {
        // Arrange
        Object stats = new Object(); // Replace with actual stats object if needed
        String[] hostnames = {"localhost"};
        RouteEntry[] routes = new RouteEntry[0]; // Replace with actual RouteEntry array if needed
        List<User> users = Collections.emptyList(); // Replace with actual User list if needed

        when(connectionManager.getManagerInfo()).thenReturn(managerInfo);

        // Act
        Heartbeat.HeartbeatEvent event = Heartbeat.get(connectionManager, stats, hostnames, routes, users);

        // Assert
        assertEquals("heartbeat", event.type());
        assertEquals(managerInfo, event.agent());
        assertEquals(stats, event.stats());
        assertEquals(hostnames, event.hostnames());
        assertEquals(routes, event.routes());
        assertEquals(users, event.users());
    }
}