package storage;

import dev.aikido.agent_api.storage.Hostnames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HostnamesTest {
    private Hostnames hostnames;

    @BeforeEach
    public void setUp() {
        hostnames = new Hostnames(3);
    }

    @Test
    public void testAddHostname() {
        hostnames.add("example.com", 80);
        Hostnames.HostnameEntry[] entries = hostnames.asArray();
        assertEquals(1, entries.length);
        assertEquals("example.com", entries[0].getHostname());
        assertEquals(80, entries[0].getPort());
        assertEquals(1, entries[0].getHits());
    }

    @Test
    public void testAddMultiplePorts() {
        hostnames.add("example.com", 80);
        hostnames.add("example.com", 443);

        Hostnames.HostnameEntry[] entries = hostnames.asArray();
        assertEquals(2, entries.length);

        assertEquals("example.com", entries[0].getHostname());
        assertEquals(80, entries[0].getPort());
        assertEquals(1, entries[0].getHits());

        assertEquals("example.com", entries[1].getHostname());
        assertEquals(443, entries[1].getPort());
        assertEquals(1, entries[1].getHits());
    }

    @Test
    public void testAddDuplicateHostname() {
        hostnames.add("example.com", 80);
        hostnames.add("example.com", 80);  // Should not change the port

        Hostnames.HostnameEntry[] entries = hostnames.asArray();
        assertEquals(1, entries.length);
        assertEquals(2, entries[0].getHits());  // Hits should increment
    }

    @Test
    public void testMaxEntries() {
        hostnames.add("example.com", 80);
        hostnames.add("test.com", 443);
        hostnames.add("localhost", 3000);
        hostnames.add("newsite.com", 8080);  // This should remove "example.com"

        Hostnames.HostnameEntry[] entries = hostnames.asArray();
        assertEquals(3, entries.length);
        assertFalse(containsEntry(entries, "example.com", 80));
        assertTrue(containsEntry(entries, "test.com", 443));
        assertTrue(containsEntry(entries, "localhost", 3000));
        assertTrue(containsEntry(entries, "newsite.com", 8080));
    }

    @Test
    public void testAsArray() {
        hostnames.add("example.com", 80);
        hostnames.add("test.com", 443);
        assertInstanceOf(Hostnames.HostnameEntry[].class, hostnames.asArray());
    }

    @Test
    public void testClear() {
        hostnames.add("example.com", 80);
        hostnames.add("test.com", 443);
        hostnames.clear();
        assertEquals(0, hostnames.asArray().length);
    }

    @Test
    public void testAddNonePort() {
        hostnames.add("example.com", 0);  // Using 0 to represent None
        Hostnames.HostnameEntry[] entries = hostnames.asArray();
        assertEquals(1, entries.length);
        assertEquals("example.com", entries[0].getHostname());
        assertEquals(0, entries[0].getPort());
        assertEquals(1, entries[0].getHits());
    }

    @Test
    public void testExceedMaxEntriesWithMultiplePorts() {
        hostnames.add("example.com", 80);
        hostnames.add("example.com", 443);
        hostnames.add("test.com", 8080);
        hostnames.add("newsite.com", 3000);  // This should remove "example.com:80"

        Hostnames.HostnameEntry[] entries = hostnames.asArray();
        assertEquals(3, entries.length);
        assertFalse(containsEntry(entries, "example.com", 80));
        assertTrue(containsEntry(entries, "example.com", 443));
        assertTrue(containsEntry(entries, "test.com", 8080));
        assertTrue(containsEntry(entries, "newsite.com", 3000));
    }

    private boolean containsEntry(Hostnames.HostnameEntry[] entries, String hostname, int port) {
        for (Hostnames.HostnameEntry entry : entries) {
            if (entry.getHostname().equals(hostname) && entry.getPort() == port) {
                return true;
            }
        }
        return false;
    }
    private int getHits(Hostnames.HostnameEntry[] entries, String hostname, int port) {
        for (Hostnames.HostnameEntry entry : entries) {
            if (entry.getHostname().equals(hostname) && entry.getPort() == port) {
                return entry.getHits();
            }
        }
        return 0; // Not found
    }
}