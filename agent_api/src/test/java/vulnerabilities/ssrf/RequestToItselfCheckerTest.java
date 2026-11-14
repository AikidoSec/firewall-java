package vulnerabilities.ssrf;

import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.vulnerabilities.ssrf.RequestToItselfChecker.isRequestToItself;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestToItselfCheckerTest {
    @Test
    public void testReturnsFalseIfServerUrlIsEmpty() {
        assertFalse(isRequestToItself("", "aikido.dev", 80, true));
    }

    @Test
    public void testReturnsFalseIfServerUrlIsInvalid() {
        assertFalse(isRequestToItself("http://", "aikido.dev", 80, true));
    }

    @Test
    public void testReturnsFalseIfPortIsDifferent() {
        assertFalse(isRequestToItself("http://aikido.dev:4000", "aikido.dev", 80, true));
        assertFalse(isRequestToItself("https://aikido.dev:4000", "aikido.dev", 443, true));
    }

    @Test
    public void testReturnsFalseIfHostnameIsDifferent() {
        assertFalse(isRequestToItself("http://aikido.dev", "google.com", 80, true));
        assertFalse(isRequestToItself("http://aikido.dev:4000", "google.com", 4000, true));
        assertFalse(isRequestToItself("https://aikido.dev", "google.com", 443, true));
        assertFalse(isRequestToItself("https://aikido.dev:4000", "google.com", 443, true));
    }

    @Test
    public void testReturnsTrueIfServerDoesRequestToItself() {
        assertTrue(isRequestToItself("https://aikido.dev", "aikido.dev", 443, true));
        assertTrue(isRequestToItself("http://aikido.dev:4000", "aikido.dev", 4000, true));
        assertTrue(isRequestToItself("http://aikido.dev", "aikido.dev", 80, true));
        assertTrue(isRequestToItself("https://aikido.dev:4000", "aikido.dev", 4000, true));
    }

    @Test
    public void testReturnsTrueForSpecialCaseHttpToHttps() {
        assertTrue(isRequestToItself("http://aikido.dev", "aikido.dev", 443, true));
        assertTrue(isRequestToItself("https://aikido.dev", "aikido.dev", 80, true));
    }

    @Test
    public void testReturnsFalseIfRequestIsNotBehindTrustedProxy() {
        assertFalse(isRequestToItself("https://aikido.dev", "aikido.dev", 443, false));
        assertFalse(isRequestToItself("http://aikido.dev", "aikido.dev", 80, false));
    }

    @Test
    public void testReturnsFalseIfServerUrlIsNull() {
        assertFalse(isRequestToItself(null, "aikido.dev", 80, true));
        assertFalse(isRequestToItself(null, "aikido.dev", 443, true));
    }

    @Test
    public void testReturnsFalseIfHostnameIsNull() {
        assertFalse(isRequestToItself("http://aikido.dev:4000", null, 80, true));
        assertFalse(isRequestToItself("https://aikido.dev:4000", null, 443, true));
    }

    @Test
    public void testReturnsFalseIfBothAreNull() {
        assertFalse(isRequestToItself(null, null, 80, true));
        assertFalse(isRequestToItself(null, null, 443, true));
    }

}
