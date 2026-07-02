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
 * SpringWebClientWrapper (URLCollector.report on ExchangeFunction.exchange) and
 * SocketChannelWrapper (DNSRecordCollector.reportConnect on SocketChannel.connect) run on
 * different threads for a real WebClient call: the former on the subscribing thread, the
 * latter on Reactor Netty's own event-loop thread. This file can't be a single cohesive test
 * the way HttpURLConnectionTest is for a same-thread blocking client, and this isn't just a
 * ThreadLocal limitation fixed by PendingHostnamesStore going global: the taint-context capture
 * (ReactorAikidoContext) only gets written into Reactor's own Context by SpringWebfluxWrapper,
 * which only fires for a *real* incoming WebFlux request - a bare "webClient.get()...block()"
 * call made directly from a test, with no request behind it, never populates it, so the SSRF
 * check silently no-ops and a real network call goes out (confirmed empirically: it hangs until
 * timeout instead of getting blocked). A true end-to-end "WebClient in a Spring app, request
 * gets blocked" test needs an actual running app - see spring_webflux_postgres.py's `ssrf`
 * payload for that. This file instead tests each wrapper's own contribution in isolation.
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
        // ExchangeFunction.exchange() runs on the subscribing thread, same as this test -
        // confirms SpringWebClientWrapper fires and calls URLCollector.report() correctly.
        assertTrue(PendingHostnamesStore.getPorts("aikido.dev").isEmpty());

        webClient.get().uri("https://aikido.dev").retrieve().bodyToMono(String.class).block();

        assertEquals(Set.of(443), PendingHostnamesStore.getPorts("aikido.dev"));
    }

    @Test
    public void testSocketChannelWrapperBlocksSsrf() throws Exception {
        // SocketChannel.connect() is synchronous and single-threaded regardless of caller, so
        // this exercises SocketChannelWrapper + DNSRecordCollector.reportConnect's real SSRF
        // logic deterministically, without Reactor's thread-hopping.
        ServiceConfigStore.updateBlocking(true);
        PendingHostnamesStore.add("localhost", 5000);
        Context.set(new EmptySampleContextObject("http://localhost:5000"));

        // Built via getByAddress (no lookup, no InetAddressWrapper interception) so the
        // resolved address reaches SocketChannel.connect() untouched, isolating this test to
        // SocketChannelWrapper's own behavior.
        InetAddress resolved = InetAddress.getByAddress("localhost", new byte[]{127, 0, 0, 1});
        InetSocketAddress address = new InetSocketAddress(resolved, 5000);
        try (SocketChannel channel = SocketChannel.open()) {
            SSRFException exception = assertThrows(SSRFException.class, () -> channel.connect(address));
            assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
        }
    }
}
