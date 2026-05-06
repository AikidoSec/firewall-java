package dev.aikido.agent_api.vulnerabilities.outbound_blocking;

import dev.aikido.agent_api.vulnerabilities.AikidoException;

public class BlockedOutboundException extends AikidoException {
    public BlockedOutboundException(String msg) {
        super(msg);
    }

    public static BlockedOutboundException get() {
        String defaultMsg = generateDefaultMessage("an outbound connection");
        return new BlockedOutboundException(defaultMsg);
    }
}
