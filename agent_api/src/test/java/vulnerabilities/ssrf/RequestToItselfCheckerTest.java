package vulnerabilities.ssrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static dev.aikido.agent_api.vulnerabilities.ssrf.RequestToItselfChecker.isRequestToItself;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestToItselfCheckerTest {
    @BeforeEach
    public void setUp() {
        System.clearProperty("AIKIDO_TRUST_PROXY");
    }

    @Test
    public void testReturnsFalseIfServerUrlIsEmpty() {
        assertFalse(isRequestToItself("", "aikido.dev", 80));
    }

    @Test
    public void testReturnsFalseIfServerUrlIsInvalid() {
        assertFalse(isRequestToItself("http://", "aikido.dev", 80));
    }

    @Test
    public void testReturnsFalseIfPortIsDifferent() {
        assertFalse(isRequestToItself("http://aikido.dev:4000", "aikido.dev", 80));
        assertFalse(isRequestToItself("https://aikido.dev:4000", "aikido.dev", 443));
    }

    @Test
    public void testReturnsFalseIfHostnameIsDifferent() {
        assertFalse(isRequestToItself("http://aikido.dev", "google.com", 80));
        assertFalse(isRequestToItself("http://aikido.dev:4000", "google.com", 4000));
        assertFalse(isRequestToItself("https://aikido.dev", "google.com", 443));
        assertFalse(isRequestToItself("https://aikido.dev:4000", "google.com", 443));
    }

    @Test
    public void testReturnsTrueIfServerDoesRequestToItself() {
        assertTrue(isRequestToItself("https://aikido.dev", "aikido.dev", 443));
        assertTrue(isRequestToItself("http://aikido.dev:4000", "aikido.dev", 4000));
        assertTrue(isRequestToItself("http://aikido.dev", "aikido.dev", 80));
        assertTrue(isRequestToItself("https://aikido.dev:4000", "aikido.dev", 4000));
    }

    @Test
    public void testReturnsTrueForSpecialCaseHttpToHttps() {
        assertTrue(isRequestToItself("http://aikido.dev", "aikido.dev", 443));
        assertTrue(isRequestToItself("https://aikido.dev", "aikido.dev", 80));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "false")
    public void testReturnsFalseIfTrustProxyIsFalse() {
        assertFalse(isRequestToItself("https://aikido.dev", "aikido.dev", 443));
        assertFalse(isRequestToItself("http://aikido.dev", "aikido.dev", 80));
    }

    @Test
    public void testReturnsFalseIfServerUrlIsNull() {
        assertFalse(isRequestToItself(null, "aikido.dev", 80));
        assertFalse(isRequestToItself(null, "aikido.dev", 443));
    }

    @Test
    public void testReturnsFalseIfHostnameIsNull() {
        assertFalse(isRequestToItself("http://aikido.dev:4000", null, 80));
        assertFalse(isRequestToItself("https://aikido.dev:4000", null, 443));
    }

    @Test
    public void testReturnsFalseIfBothAreNull() {
        assertFalse(isRequestToItself(null, null, 80));
        assertFalse(isRequestToItself(null, null, 443));
    }

}
