package dev.aikido.agent_api.helpers;

import dev.aikido.agent_api.helpers.net.IPList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IPListBuilder {
    private IPListBuilder() {}

    public static IPList createIPList(Collection<String> ips) {
        return new IPList(ips);
    }

    public static IPList createIPListWithMappedAddresses(Collection<String> ips) {
        if (ips == null || ips.isEmpty()) {
            return new IPList(ips);
        }

        List<String> addresses = new ArrayList<>(ips);
        for (String ip : ips) {
            String mappedAddress = mapIPv4ToIPv6(ip);
            if (mappedAddress != null) {
                addresses.add(mappedAddress);
            }
        }
        return new IPList(addresses);
    }

    private static String mapIPv4ToIPv6(String ip) {
        if (ip == null || ip.contains(":")) {
            return null;
        }

        int slash = ip.indexOf('/');
        if (slash < 0) {
            return "::ffff:" + ip + "/128";
        }

        int prefix = 0;
        int position = slash + 1;
        int prefixStart = position;
        while (position < ip.length()) {
            char character = ip.charAt(position);
            if (character < '0' || character > '9') {
                break;
            }
            prefix = prefix * 10 + character - '0';
            if (prefix > 32) {
                return null;
            }
            position++;
        }
        if (position == prefixStart) {
            return null;
        }
        return "::ffff:" + ip.substring(0, slash) + "/" + (prefix + 96);
    }
}
