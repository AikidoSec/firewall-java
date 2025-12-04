package dev.aikido.agent_api.helpers.extraction;

public class IPV6BracketsHelper {
    public static String removeIfExistsIPv6Brackets(String ip) {
        if (ip.startsWith("[") && ip.endsWith("]")) {
            return ip.substring(1, ip.length()-1);
        }
        return ip;
    }
}
