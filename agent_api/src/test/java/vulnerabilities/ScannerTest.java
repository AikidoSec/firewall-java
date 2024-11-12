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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScannerTest {

    private ContextObject mockContext;
    private Vulnerabilities.Vulnerability mockVulnerability;
    private Detector mockDetector;
    private IPCClient mockIPCClient;

    @BeforeEach
    void setUp() {
        mockContext = mock(ContextObject.class);
        mockVulnerability = mock(Vulnerabilities.Vulnerability.class);
        mockDetector = mock(Detector.class);
        mockIPCClient = mock(IPCClient.class);

        // Set up the context to return the mock context object
        Context.set(mockContext);
        // Set up the vulnerability to return the mock detector
        when(mockVulnerability.getDetector()).thenReturn(mockDetector);
    }

    @Test
    void testScanForGivenVulnerability_ContextIsNull() {
        Context.set(null);
        Scanner.scanForGivenVulnerability(mockVulnerability, "operation", new String[]{"arg1"});

        // Verify that no interactions occur when context is null
        verifyNoInteractions(mockDetector, mockIPCClient);
    }

    @Test
    void testScanForGivenVulnerability_NoAttackDetected() {
        when(mockDetector.run(anyString(), any())).thenReturn(new Detector.DetectorResult(false, null, null));

        Scanner.scanForGivenVulnerability(mockVulnerability, "operation", new String[]{"arg1"});

        // Verify that no interactions occur with IPCClient
        verify(mockIPCClient, never()).sendData(anyString(), anyBoolean());
    }
}