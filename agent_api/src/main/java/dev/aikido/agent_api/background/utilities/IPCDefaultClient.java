package dev.aikido.agent_api.background.utilities;

import dev.aikido.agent_api.helpers.env.Token;

public class IPCDefaultClient extends IPCClient{
    public IPCDefaultClient() {
        super(UDSPath.getUDSPath(Token.fromEnv()));
    }
}
