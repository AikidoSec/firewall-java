package dev.aikido.agent_api.helpers;

import dev.aikido.agent_api.helpers.net.IPList;

import java.util.Collection;

public final class IPListBuilder {
    private IPListBuilder() {}

    public static IPList createIPList(Collection<String> ips) {
        return new IPList(ips);
    }
}
