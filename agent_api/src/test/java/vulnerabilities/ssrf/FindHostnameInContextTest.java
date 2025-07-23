package vulnerabilities.ssrf;

import dev.aikido.agent_api.vulnerabilities.ssrf.FindHostnameInContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FindHostnameInContextTest {
    public static boolean hostnameInUserInput(String userInput, String hostname, int port) {
        return FindHostnameInContext.hostnameInUserInput(userInput, FindHostnameInContext.getHostnameOptions(hostname), port);
    }
        @Test
        void testReturnsFalseIfUserInputAndHostnameAreEmpty() {
            assertFalse(hostnameInUserInput("", "", 80));
        }

        @Test
        void testReturnsFalseIfUserInputIsEmpty() {
            assertFalse(hostnameInUserInput("", "example.com", 80));
        }

        @Test
        void testReturnsFalseIfHostnameIsEmpty() {
            assertFalse(hostnameInUserInput("http://example.com", "", 80));
        }

        @Test
        void testItParsesHostnameFromUserInput() {
            assertTrue(hostnameInUserInput("http://localhost", "localhost", 80));
        }

        @Test
        void testItParsesSpecialIp() {
            assertTrue(hostnameInUserInput("http://localhost", "localhost", 80));
        }

        @Test
        void testItParsesHostnameFromUserInputWithPathBehindIt() {
            assertTrue(hostnameInUserInput("http://localhost/path", "localhost", 80));
        }

        @Test
        void testHostnameCapitalizationIsNotImportant() {
            assertTrue(hostnameInUserInput("http://Localhost/path", "localhost", 80));
        }

        @Test
        void testHostnameCapitalizationIsNotImportant2() {
            assertTrue(hostnameInUserInput("http://localhost/path", "LOCALHOST", 80));
        }


        @Test
        void testItDoesNotParseHostnameFromUserInputWithMisspelledProtocol() {
            assertFalse(hostnameInUserInput("http:/localhost", "localhost", 80));
        }

        @Test
        void testItDoesNotParseHostnameFromUserInputWithoutProtocolSeparator() {
            assertFalse(hostnameInUserInput("http:localhost", "localhost", 80));
        }

        @Test
        void testItDoesNotParseHostnameFromUserInputWithMisspelledProtocolAndPathBehindIt() {
            assertFalse(hostnameInUserInput("http:/localhost/path/path", "localhost", 80));
        }

        @Test
        void testItParsesHostnameFromUserInputWithoutProtocolAndPathBehindIt() {
            assertTrue(hostnameInUserInput("localhost/path/path", "localhost", 80));
        }

        @Test
        void testItFlagsFtpAsProtocol() {
            assertTrue(hostnameInUserInput("ftp://localhost", "localhost", 80));
        }

        @Test
        void testItParsesHostnameFromUserInputWithoutProtocol() {
            assertTrue(hostnameInUserInput("localhost", "localhost", 80));
        }

        @Test
        void testItIgnoresInvalidUrls() {
            assertFalse(hostnameInUserInput("http://", "localhost", 80));
        }

        @Test
        void testUserInputIsSmallerThanHostname() {
            assertFalse(hostnameInUserInput("localhost", "localhost localhost", 80));
        }

        @Test
        void testItFindsIpAddressInsideUrl() {
            assertTrue(hostnameInUserInput("http://169.254.169.254/latest/meta-data/", "169.254.169.254", 80));
        }

        @Test
        void testItFindsIpAddressWithStrangeNotationInsideUrl() {
            assertTrue(hostnameInUserInput("http://2130706433", "2130706433", 80));
            assertTrue(hostnameInUserInput("http://127.0.0.1", "127.0.0.1", 80));
        }

        @Test
        void testMoreComplexIPv6RequiringBrackets() {
            assertTrue(hostnameInUserInput("http://[0000:0000:0000:0000:0000:0000:0000:0001]:8081", "0000:0000:0000:0000:0000:0000:0000:0001", 8081));
            assertTrue(hostnameInUserInput("http://[::ffff:127.0.0.1]", "::ffff:127.0.0.1", 80));
            assertTrue(hostnameInUserInput("http://[::1]", "::1", 80));
            assertTrue(hostnameInUserInput("http://[::ffff:127.0.0.1]", "::ffff:127.0.0.1", 80));
            assertFalse(hostnameInUserInput("http://[::ffff:127.0.0.1]:81", "::ffff:127.0.0.1", 80));
            assertFalse(hostnameInUserInput("http://[::ffff:127.0.0.1]:80", "::ffff:127.0.0.1", 443));
            assertFalse(hostnameInUserInput("http://::1", "::1", 80));
        }

        @Test
        void testItWorksWithInvalidPorts() {
            assertTrue(hostnameInUserInput("http://localhost:1337/u0000asd.php", "localhost", 1337));
        }

        @Test
        void testItWorksWithPorts() {
            assertFalse(hostnameInUserInput("http://localhost:80", "localhost", 8080));
            assertTrue(hostnameInUserInput("http://localhost:8080", "localhost", 8080));
            assertFalse(hostnameInUserInput("http://localhost:8080", "localhost", 80));
            assertFalse(hostnameInUserInput("http://localhost:8080", "localhost", 4321));
        }

}
