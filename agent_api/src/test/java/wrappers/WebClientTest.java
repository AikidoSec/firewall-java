package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.PendingHostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import utils.EmptySampleContextObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SpringWebClientWrapper and SocketChannelWrapper fire on different threads for a real
 * WebClient call (subscribing thread vs. Reactor Netty's event loop), and
 * PendingHostnamesStore/Context are ThreadLocal - so unlike HttpURLConnectionTest, this can't
 * be one cohesive test. Each wrapper's contribution is tested separately instead.
 */
public class WebClientTest {
    private static final WebClient webClient = WebClient.create();

    @AfterEach
    void cleanup() {
        Context.set(null);
        HostnamesStore.clear();
        PendingHostnamesStore.clear();
    }

    @BeforeEach
    void beforeEach() {
        cleanup();
        ServiceConfigStore.updateBlocking(true);
        PendingHostnamesStore.clear();
    }

    @Test
    public void testSpringWebClientWrapperRegistersPendingPort() {
        assertTrue(PendingHostnamesStore.getPorts("aikido.dev").isEmpty());

        webClient.get().uri("https://aikido.dev").retrieve().bodyToMono(String.class).block();

        assertEquals(Set.of(443), PendingHostnamesStore.getPorts("aikido.dev"));
    }

    @Test
    public void testSocketChannelWrapperBlocksSsrf() throws Exception {
        ServiceConfigStore.updateBlocking(true);
        PendingHostnamesStore.add("localhost", 5000);
        Context.set(new EmptySampleContextObject("http://localhost:5000"));

        // getByAddress avoids InetAddressWrapper interception, isolating this to SocketChannelWrapper.
        InetAddress resolved = InetAddress.getByAddress("localhost", new byte[]{127, 0, 0, 1});
        InetSocketAddress address = new InetSocketAddress(resolved, 5000);
        try (SocketChannel channel = SocketChannel.open()) {
            SSRFException exception = assertThrows(SSRFException.class, () -> channel.connect(address));
            assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
        }
    }
}
