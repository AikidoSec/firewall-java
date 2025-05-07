package dev.aikido.agent_api.helpers;

import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.vulnerabilities.AikidoException;

public final class OutboundConnectionChecker {
    private OutboundConnectionChecker() {
    }

    public static class BlockedOutboundConnection extends AikidoException {
        public BlockedOutboundConnection(String msg) {
            super(msg);
        }
    }

    public static void checkDomain(String hostname, int port) {
        // check domain against list of blocked domains

        boolean blocked = true;
        if (blocked) {
            HostnamesStore.incrementBlockedHits();
            throw new BlockedOutboundConnection("Connection not allowed to " + hostname + ":" + port);
        }
    }
}
