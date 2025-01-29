package dev.aikido.agent_api.storage;

import java.util.LinkedHashMap;
import java.util.Map;

public class Hostnames {
    private final Map<String, HostnameEntry> map;

    public Hostnames(int maxEntries) {
        this.map = new LinkedHashMap<>(maxEntries, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, HostnameEntry> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public void add(String hostname, int port) {
        String key = getKey(hostname, port);
        if (!map.containsKey(key)) {
            map.put(key, new HostnameEntry(hostname, port));
        }
        map.get(key).incrementHits();
    }
    public void addArray(HostnameEntry[] hostnameEntries) {
        for (HostnameEntry entry: hostnameEntries) {
            add(entry.getHostname(), entry.getPort());
        }
    }
    public HostnameEntry[] asArray() {
        return map.values().toArray(new HostnameEntry[0]);
    }

    public void clear() {
        map.clear();
    }

    private String getKey(String hostname, int port) {
        return hostname + ":" + port;
    }

    public static class HostnameEntry {
        private final String hostname;
        private final int port;
        private int hits;

        public HostnameEntry(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
            this.hits = 0;
        }

        public void incrementHits() {
            hits++;
        }

        public String getHostname() {
            return hostname;
        }

        public int getPort() {
            return port;
        }

        public int getHits() {
            return hits;
        }
    }
}
