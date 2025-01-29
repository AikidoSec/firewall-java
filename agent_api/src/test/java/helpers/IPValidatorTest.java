package helpers;

import static org.junit.jupiter.api.Assertions.*;

import dev.aikido.agent_api.helpers.net.IPValidator;
import org.junit.jupiter.api.Test;

public class IPValidatorTest {

    @Test
    public void testValidateIPAddresses() {
        String[] validIPs = {
            "127.0.0.1",
            "0.0.0.0",
            "255.255.255.255",
            "1.2.3.4",
            "::1",
            "2001:db8:0000:1:1:1:1:1",
            "2001:db8:3:4::192.0.2.33",
            "2001:41d0:2:a141::1",
            "::ffff:127.0.0.1",
            "::0000",
            "0000::",
            "1::",
            "1111:1:1:1:1:1:1:1",
            "fe80::a6db:30ff:fe98:e946",
            "::",
            "::8",
            "::ffff:127.0.0.1",
            "::ffff:255.255.255.255",
            "::ffff:0:255.255.255.255",
            "::2:3:4:5:6:7:8",
            "::255.255.255.255",
            "0:0:0:0:0:ffff:127.0.0.1",
            "1:2:3:4:5:6:7::",
            "1:2:3:4:5:6::8",
            "1::7:8",
            "1:2:3:4:5::7:8",
            "1:2:3:4:5::8",
            "1::6:7:8",
            "1:2:3:4::6:7:8",
            "1:2:3:4::8",
            "1::5:6:7:8",
            "1:2:3::5:6:7:8",
            "1:2:3::8",
            "1::4:5:6:7:8",
            "1:2::4:5:6:7:8",
            "1:2::8",
            "1::3:4:5:6:7:8",
            "1::8",
            "fe80::7:8%eth0",
            "fe80::7:8%1",
            "64:ff9b::192.0.2.33",
            "0:0:0:0:0:0:10.0.0.1"
        };

        for (String ip : validIPs) {
            assertTrue(IPValidator.isIP(ip, ""), "Expected valid IP: " + ip);
        }

        String[] invalidIPs = {
            "abc",
            "256.0.0.0",
            "0.0.0.256",
            "26.0.0.256",
            "0200.200.200.200",
            "200.0200.200.200",
            "200.200.0200.200",
            "200.200.200.0200",
            "::banana",
            "banana::",
            "::1banana",
            "::1::",
            "1:",
            ":1",
            ":1:1:1::2",
            "1:1:1:1:1:1:1:1:1:1:1:1:1:1:1:1",
            "::11111",
            "11111:1:1:1:1:1:1:1",
            "2001:db8:0000:1:1:1:1::1",
            "0:0:0:0:0:0:ffff:127.0.0.1",
            "0:0:0:0:ffff:127.0.0.1"
        };
        for (String ip : invalidIPs) {
            assertFalse(IPValidator.isIP(ip, ""), "Expected invalid IP: " + ip);
        }
    }

    @Test
    public void testValidateIPv4Addresses() {
        String[] validIPv4 = {"127.0.0.1", "0.0.0.0", "255.255.255.255", "1.2.3.4", "255.0.0.1", "0.0.1.1"};

        for (String ip : validIPv4) {
            assertTrue(IPValidator.isIP(ip, "4"), "Expected valid IPv4: " + ip);
        }

        String[] invalidIPv4 = {
            "::1", "2001:db8:0000:1:1:1:1:1", "::ffff:127.0.0.1",
            "137.132.10.01", "0.256.0.256", "255.256.255.256"
        };

        for (String ip : invalidIPv4) {
            assertFalse(IPValidator.isIP(ip, "4"), "Expected invalid IPv4: " + ip);
        }
    }

    @Test
    public void testValidateIPv6Addresses() {
        String[] validIPv6 = {
            "::1", "2001:db8:0000:1:1:1:1:1", "::ffff:127.0.0.1",
            "fe80::1234%1", "ff08::9abc%10", "ff08::9abc%interface10",
            "ff02::5678%pvc1.3"
        };

        for (String ip : validIPv6) {
            assertTrue(IPValidator.isIP(ip, "6"), "Expected valid IPv6: " + ip);
        }

        String[] invalidIPv6 = {
            "127.0.0.1",
            "0.0.0.0",
            "255.255.255.255",
            "1.2.3.4",
            "::ffff:287.0.0.1",
            "%",
            "fe80::1234%",
            "fe80::1234%1%3%4",
            "fe80%fe80%"
        };

        for (String ip : invalidIPv6) {
            assertFalse(IPValidator.isIP(ip, "6"), "Expected invalid IPv6: " + ip);
        }
    }

    @Test
    public void testValidateInvalidVersion() {
        String[] invalidVersionIPs = {
            "127.0.0.1", "0.0.0.0", "255.255.255.255", "1.2.3.4", "::1", "2001:db8:0000:1:1:1:1:1"
        };

        for (String ip : invalidVersionIPs) {
            assertFalse(IPValidator.isIP(ip, "10"), "Expected invalid IP for version 10: " + ip);
        }
    }
}
