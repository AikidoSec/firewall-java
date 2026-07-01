package dev.aikido.agent_api.storage;

import java.util.*;

/**
 * Thread-local bridge between URLCollector and DNSRecordCollector.
 * URLCollector records hostname+port here; DNSRecordCollector.report() (fed by
 * InetAddress.getAllByName(), which resolves everything in one call) reads and removes the
 * entry so each (hostname, port) pair is processed exactly once per DNS lookup.
 * DNSRecordCollector.reportConnect() (fed by SocketChannel.connect(), which fires once per
 * connect attempt) instead peeks the entry, since a single outbound request can trigger
 * multiple connect attempts to the same hostname (e.g. IPv4 then IPv6 for a dual-stack host).
 *
 * Entries are normally cleared per incoming request by WebRequestCollector, but a peeked
 * entry added outside any incoming-request context (e.g. a WebClient call from a @Scheduled
 * task) would never be cleared that way. Capped at MAX_ENTRIES per thread, evicting the least
 * recently used entry once exceeded, same bounded-LRU pattern as Hostnames.
 */
public final class PendingHostnamesStore {
    private PendingHostnamesStore() {}

    private static final int MAX_ENTRIES = 1000;

    private static final ThreadLocal<Map<String, Set<Integer>>> store =
            ThreadLocal.withInitial(() -> new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Set<Integer>> eldest) {
                    return size() > MAX_ENTRIES;
                }
            });

    public static void add(String hostname, int port) {
        Map<String, Set<Integer>> map = store.get();
        if (!map.containsKey(hostname)) {
            map.put(hostname, new LinkedHashSet<>());
        }
        map.get(hostname).add(port);
    }

    public static Set<Integer> getAndRemove(String hostname) {
        Set<Integer> ports = store.get().remove(hostname);
        if (ports == null) {
            return Collections.emptySet();
        }
        return ports;
    }

    public static Set<Integer> getPorts(String hostname) {
        Set<Integer> ports = store.get().get(hostname);
        if (ports == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(ports);
    }

    public static void clear() {
        store.get().clear();
    }
}
