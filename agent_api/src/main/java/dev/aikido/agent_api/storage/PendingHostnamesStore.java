package dev.aikido.agent_api.storage;

import java.util.*;

/**
 * Thread-local bridge between URLCollector and DNSRecordCollector.
 * URLCollector records hostname+port here; DNSRecordCollector reads and removes the entry
 * so each (hostname, port) pair is processed exactly once per DNS lookup.
 */
public final class PendingHostnamesStore {
    private PendingHostnamesStore() {}

    private static final ThreadLocal<Map<String, Set<Integer>>> store =
            ThreadLocal.withInitial(LinkedHashMap::new);

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
