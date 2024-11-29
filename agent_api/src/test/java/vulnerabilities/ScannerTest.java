package vulnerabilities;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.utilities.IPCClient;
import dev.aikido.agent_api.background.utilities.IPCDefaultClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    @BeforeEach
    void setUp() {
        Context.set(new SampleContextObject());
    }
    @AfterEach
    void cleanup() {
        Context.set(null);
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
}