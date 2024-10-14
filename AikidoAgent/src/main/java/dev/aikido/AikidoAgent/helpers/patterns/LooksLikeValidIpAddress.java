package dev.aikido.AikidoAgent.helpers.patterns;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LooksLikeValidIpAddress {
    public static boolean isValidIpAddress(String segment) {
        try {
            InetAddress.getByName(segment);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
