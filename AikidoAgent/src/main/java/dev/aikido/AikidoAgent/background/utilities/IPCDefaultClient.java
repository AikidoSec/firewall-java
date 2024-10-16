package dev.aikido.AikidoAgent.background.utilities;

import dev.aikido.AikidoAgent.helpers.env.Token;

public class IPCDefaultClient extends IPCClient{
    public IPCDefaultClient() {
        super(UDSPath.getUDSPath(Token.fromEnv()));
    }
}
