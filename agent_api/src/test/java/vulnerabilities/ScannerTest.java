package vulnerabilities;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ConfigStore;
import dev.aikido.agent_api.vulnerabilities.Detector;
import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import dev.aikido.agent_api.vulnerabilities.sql_injection.SQLInjectionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static utils.EmptyAPIResponses.emptyAPIResponse;
import static utils.EmptyAPIResponses.setEmptyConfigWithEndpointList;

class ScannerTest {
    public static class SampleContextObject2 extends EmptySampleContextObject {
        public SampleContextObject2(String ip) {
            super("SELECT * FRO");
            this.remoteAddress = ip;
        }
    }
    public static class SampleContextObject3 extends EmptySampleContextObject {
        public SampleContextObject3(String route) {
            super("SELECT * FRO");
            this.route = route;
            this.url = "http://localhost:5050" + route;
        }
    }
    @BeforeEach
    void setUp() {
        Context.set(new EmptySampleContextObject("SELECT * FRO"));
        setEmptyConfigWithEndpointList(List.of(
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
        ));
    }
    @AfterEach
    void cleanup() {
        Context.set(null);
        ConfigStore.updateFromAPIResponse(emptyAPIResponse);
    }

    @Test
    void testScanForGivenVulnerability_ContextIsNull() {
        Vulnerabilities.Vulnerability mockVulnerability = mock(Vulnerabilities.Vulnerability.class);
        Detector mockDetector = mock(Detector.class);
        when(mockVulnerability.getDetector()).thenReturn(mockDetector);
        Context.set(null);
        Scanner.scanForGivenVulnerability(mockVulnerability, "operation", new String[]{"arg1"});

        // Verify that no interactions occur when context is null
        verify(mockDetector, times(1)).returnEarly(new String[]{"arg1"}); // Verify returnEarly is called once
        verifyNoMoreInteractions(mockDetector); // Verify no other methods are
    }

    @Test
    void testScanSafeSQLCode() {
        ConfigStore.updateBlocking(true);
        // Safe :
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT", "postgresql"});
        // Argument-mismatch, safe :
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM"});
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "1", "2", "3"});

        // Unsafe :
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });
    }

    @Test
    void testScanSafeSQLCodeButBlockingFalse() {
        ConfigStore.updateBlocking(false);
        // Safe :
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT", "postgresql"});
        // Argument-mismatch, safe :
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM"});
        Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "1", "2", "3"});

        // Unsafe :
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });
    }

    @Test
    void testForceProtectionOff() {
        ConfigStore.updateBlocking(true);
        // Thread cache does not force any protection off :
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });
        // Set to protection forced off route :
        Context.set(new SampleContextObject3("/api2/test/2/4"));
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });
        Context.set(new SampleContextObject3("/api2/"));
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });

        // Set to IP where route exists but protection is not forced off :
        Context.set(new SampleContextObject3("/api3/test"));
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });
    }

    @Test
    void testDoesNotRunWithContextNull() {
        ConfigStore.updateBlocking(true);
        Context.set(null);
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });
    }

    @Test
    void TestStillThrowsWithConfigStoreEmptyButBlockingEnabled() {
        ConfigStore.updateFromAPIResponse(emptyAPIResponse);
        ConfigStore.updateBlocking(true);
        assertThrows(SQLInjectionException.class, () -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });
    }

    @Test
    void TestDoesNotThrowWithEmptyAPIResponse() {
        ConfigStore.updateFromAPIResponse(emptyAPIResponse);
        assertDoesNotThrow(() -> {
            Scanner.scanForGivenVulnerability(new Vulnerabilities.SQLInjectionVulnerability(), "operation", new String[]{"SELECT * FROM", "postgresql"});
        });
    }
}