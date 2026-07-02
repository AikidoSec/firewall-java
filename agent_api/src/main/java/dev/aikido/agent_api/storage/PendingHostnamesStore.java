package dev.aikido.agent_api.storage;

import java.util.*;

/**
 * Thread-local bridge between URLCollector and DNSRecordCollector.
 * report() consumes the entry (InetAddress.getAllByName() resolves everything in one call);
 * reportConnect() peeks it instead, since one request can trigger multiple connect() attempts
 * to the same hostname (e.g. IPv4 then IPv6) and consuming would skip SSRF on later attempts.
 * Capped at MAX_ENTRIES per thread with LRU eviction, same pattern as Hostnames - a peeked
 * entry outside any incoming-request context (e.g. a @Scheduled task) never gets cleared
 * otherwise.
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
