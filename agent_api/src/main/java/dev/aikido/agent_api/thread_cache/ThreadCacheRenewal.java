package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.ipc_commands.Command;
import dev.aikido.agent_api.background.ipc_commands.SyncDataCommand;
import dev.aikido.agent_api.background.utilities.ThreadClient;

import java.util.Optional;

import static dev.aikido.agent_api.background.utilities.ThreadClientFactory.getDefaultThreadClient;

public final class ThreadCacheRenewal {
    private ThreadCacheRenewal() {}
    public static ThreadCacheObject renewThreadCache() {
        // Fetch thread cache over IPC:
        ThreadClient client = getDefaultThreadClient();
        if (client == null) {
            return null;
        }
        Optional<SyncDataCommand.Res> result = new SyncDataCommand().send(client, new Command.EmptyResult());
        if(result.isPresent()) {
            SyncDataCommand.Res res = result.get();
            if (res != null) {
                return new ThreadCacheObject(res.endpoints(), res.blockedUserIDs(), res.bypassedIPs(), res.routes());
            }
        }
        return null;
    }
}
