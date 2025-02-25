package dev.aikido.agent_api.helpers;

import dev.aikido.agent_api.helpers.net.IPList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class IPListBuilder {
    private IPListBuilder() {}
    public static IPList createIPList(Collection<String> ips) {
        IPList ipList = new IPList();
        for (String ip: ips) {
            // Add ip address or subnet to IP list :
            ipList.add(ip);
        }

        return ipList;
    }
}
