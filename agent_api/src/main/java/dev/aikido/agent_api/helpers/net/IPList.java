package dev.aikido.agent_api.helpers.net;

import java.util.Collection;
import java.util.List;

public class IPList {
    private IPMatcher matcher;

    public IPList() {
        matcher = IPMatcher.from(List.of());
    }

    public IPList(Collection<String> ipAddresses) {
        matcher = IPMatcher.from(ipAddresses);
    }

    public void add(String ipOrCIDR) {
        matcher = matcher.add(ipOrCIDR);
    }

    public boolean matches(String ip) {
        return matcher.matches(ip);
    }

    public boolean matchesWithMappedCheck(String ip) {
        return matcher.matchesWithMappedCheck(ip);
    }

    public int length() {
        return matcher.size();
    }
}
