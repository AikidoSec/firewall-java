package vulnerabilities;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.utilities.IPCClient;
import dev.aikido.agent_api.background.utilities.IPCDefaultClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import dev.aikido.agent_api.vulnerabilities.Detector;
import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import dev.aikido.agent_api.vulnerabilities.sql_injection.SQLInjectionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScannerTest {

    public static class SampleContextObject extends ContextObject {
        public SampleContextObject() {
            this.method = "GET";
            this.source = "web";
            this.url = "https://example.com/api/resource";
            this.route = "/api/resource";
            this.remoteAddress = "192.168.1.1";
            this.headers = new HashMap<>();

            this.query = new HashMap<>();
            this.query.put("search", new String[]{"example", "dev.aikido:80"});
            this.query.put("sql1", new String[]{"SELECT * FRO"});

            this.cookies = new HashMap<>();
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
        }
    }
    public static class SampleContextObject2 extends SampleContextObject {
        public SampleContextObject2(String ip) {
            super();
            this.remoteAddress = ip;
        }
    }
    public static class SampleContextObject3 extends SampleContextObject {
        public SampleContextObject3(String route) {
            super();
            this.route = route;
            this.url = "http://localhost:5050" + route;
        }
    }
    private ThreadCacheObject threadCacheObject;
    @BeforeEach
    void setUp() {
        threadCacheObject = new ThreadCacheObject(
            List.of(
                    new Endpoint(
                        /* method */ "*", /* route */ "/api2/*",
                        /* rlm params */ 0, 0,
                        /* Allowed IPs */ List.of(), /* graphql */ false,
                        /* forceProtectionOff */ true, /* rlm */ false
                    ),
                    new Endpoint(
                        /* method */ "*", /* route */ "/api3/*",
                        /* rlm params */ 0, 0,
                        /* Allowed IPs */ List.of(), /* graphql */ false,
                        /* forceProtectionOff */ false, /* rlm */ false
                    )
            ),
            Set.of(),
            Set.of("1.1.1.1", "2.2.2.2", "3.3.3.3"),
            new Routes()
        );
        Context.set(new SampleContextObject());
        ThreadCache.set(threadCacheObject);
    }
    @AfterEach
    void cleanup() {
        Context.set(null);
        ThreadCache.set(null);
    }

    @Test
    void testScanForGivenVulnerability_ContextIsNull() {
        Vulnerabilities.Vulnerability mockVulnerability = mock(Vulnerabilities.Vulnerability.class);
        Detector mockDetector = mock(Detector.class);
        when(mockVulnerability.getDetector()).thenReturn(mockDetector);
        Context.set(null);
        Scanner.scanForGivenVulnerability(mockVulnerability, "operation", new String[]{"arg1"});

        // Verify that no interactions occur when context is null
        verifyNoInteractions(mockDetector);
    }

    // Disable IPC :
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "improper-access-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    void testScanSafeSQLCode() {
        // Safe :
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT", "postgres"});
        // Argument-mismatch, safe :
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM"});
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "1", "2", "3"});

        // Unsafe :
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
    }

    // Disable IPC :
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "improper-access-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    void testBypassedIPs() {
        // Thread cache does not force any protection off :
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
        // Add IP to bypassed IP's :
        Context.set(new SampleContextObject2("1.1.1.1"));
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
        Context.set(new SampleContextObject2("3.3.3.3"));
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });

        // Set to IP where protection is not forced off :
        Context.set(new SampleContextObject2("6.6.6.6"));
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });

        // Set to bypassed IP, but first reset the thread cache :
        Context.set(new SampleContextObject2("1.1.1.1"));
        ThreadCache.set(null);
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
    }

    // Disable IPC :
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "improper-access-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    void testForceProtectionOff() {
        // Thread cache does not force any protection off :
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
        // Set to protection forced off route :
        Context.set(new SampleContextObject3("/api2/test/2/4"));
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
        Context.set(new SampleContextObject3("/api2/"));
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });

        // Set to IP where route exists but protection is not forced off :
        Context.set(new SampleContextObject3("/api3/test"));
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "improper-access-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    void testDoesNotRunWithContextNull() {
        Context.set(null);
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "improper-access-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    void TestStillThrowsWithThreadCacheUndefined() {
        ThreadCache.set(null);
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgres"});
        });
    }
}