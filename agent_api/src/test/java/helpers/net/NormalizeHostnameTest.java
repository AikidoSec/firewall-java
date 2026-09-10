package helpers.net;

import dev.aikido.agent_api.helpers.net.NormalizeHostname;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NormalizeHostnameTest {

    @Test
    void testNullAndEmpty() {
        assertNull(NormalizeHostname.normalize(null));
        assertEquals("", NormalizeHostname.normalize(""));
    }

    @Test
    void testLowercases() {
        assertEquals("example.com", NormalizeHostname.normalize("EXAMPLE.COM"));
        assertEquals("metadata.google.internal", NormalizeHostname.normalize("METADATA.GOOGLE.INTERNAL"));
    }

    @Test
    void testStripsTrailingDot() {
        assertEquals("example.com", NormalizeHostname.normalize("example.com."));
        assertEquals("metadata.google.internal", NormalizeHostname.normalize("metadata.google.internal."));
        assertEquals("metadata.goog", NormalizeHostname.normalize("metadata.goog."));
    }

    @Test
    void testStripsTrailingDotAndLowercases() {
        assertEquals("metadata.google.internal", NormalizeHostname.normalize("METADATA.GOOGLE.INTERNAL."));
    }

    @Test
    void testPlainHostnameUnchanged() {
        assertEquals("example.com", NormalizeHostname.normalize("example.com"));
        assertEquals("localhost", NormalizeHostname.normalize("localhost"));
    }
}
