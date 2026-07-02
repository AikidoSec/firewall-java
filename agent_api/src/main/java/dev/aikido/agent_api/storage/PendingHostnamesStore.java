package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;

import java.util.*;

/**
 * Bridge between URLCollector and DNSRecordCollector.
 * URLCollector records hostname+port here; DNSRecordCollector.report() (fed by
 * InetAddress.getAllByName(), which resolves everything in one call) reads and removes the
 * entry so each (hostname, port) pair is processed exactly once per DNS lookup.
 * DNSRecordCollector.reportConnect() (fed by SocketChannel.connect(), which fires once per
 * connect attempt) instead peeks the entry, since a single outbound request can trigger
 * multiple connect attempts to the same hostname (e.g. IPv4 then IPv6 for a dual-stack host).
 *
 * Global rather than thread-local: for async clients (e.g. Spring's WebClient/Reactor Netty),
 * the intent registration and the actual connect can run on different OS threads - Reactor
 * Netty's own event-loop dispatch, or an app's explicit .publishOn() - so thread-local storage
 * silently loses the entry. Trade-off: two concurrent requests to the *same* hostname can share
 * an entry (and its captured context) in a narrow race window. This doesn't open an SSRF bypass
 * (SSRFDetector/StoredSSRFDetector still run unconditionally either way) - worst case is a wrong
 * source attribution for that one request.
 *
 * Capped at MAX_ENTRIES, evicting the least recently used entry once exceeded, so it can't grow
 * unboundedly under load or from entries that are never consumed (e.g. a WebClient call from a
 * @Scheduled task, outside any incoming-request context).
 */
public final class PendingHostnamesStore {
    private PendingHostnamesStore() {}

    private static final int MAX_ENTRIES = 1000;

    private record Entry(Set<Integer> ports, ContextObject context) {}

    private static final Map<String, Entry> store =
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
                    return size() > MAX_ENTRIES;
                }
            });

    public static void add(String hostname, int port) {
        add(hostname, port, Context.get());
    }

    // Used where the caller already resolved the correct context itself (e.g. via Reactor's own
    // Context, which - unlike Context.get()'s ThreadLocal - survives scheduler hops).
    public static void add(String hostname, int port, ContextObject context) {
        synchronized (store) {
            Entry existing = store.get(hostname);
            if (existing == null) {
                Set<Integer> ports = new LinkedHashSet<>();
                ports.add(port);
                store.put(hostname, new Entry(ports, context));
            } else {
                existing.ports().add(port);
            }
        }
    }

    public static Set<Integer> getAndRemove(String hostname) {
        synchronized (store) {
            Entry entry = store.remove(hostname);
            return entry == null ? Collections.emptySet() : entry.ports();
        }
    }

    public static Set<Integer> getPorts(String hostname) {
        synchronized (store) {
            Entry entry = store.get(hostname);
            return entry == null ? Collections.emptySet() : Set.copyOf(entry.ports());
        }
    }

    // The ContextObject captured when this hostname's pending entry was registered, so SSRF
    // taint-checking can use the request that actually triggered the outbound call even if it
    // runs on a different thread than the one processing the connect. Null if none was captured.
    public static ContextObject getContext(String hostname) {
        synchronized (store) {
            Entry entry = store.get(hostname);
            return entry == null ? null : entry.context();
        }
    }

    public static void clear() {
        store.clear();
    }
}
