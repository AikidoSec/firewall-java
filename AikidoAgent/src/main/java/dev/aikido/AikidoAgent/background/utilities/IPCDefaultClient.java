package dev.aikido.AikidoAgent.background.utilities;

public class IPCDefaultClient extends IPCClient{
    public IPCDefaultClient() {
        super(UDSPath.getUDSPath());
    }
}
