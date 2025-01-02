package dev.aikido.agent_api.thread_cache;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.ipc_commands.SyncDataCommand;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;

import java.util.Optional;

import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;

public final class ThreadCacheRenewal {
    private ThreadCacheRenewal() {}
    public static ThreadCacheObject renewThreadCache() {
        // Fetch thread cache over IPC:
        ThreadIPCClient client = getDefaultThreadIPCClient();
        if (client == null) {
            return null;
        }
        Optional<String> result = client.send("SYNC_DATA$", true);
        if(result.isPresent()) {
            Gson gson = new Gson();
            SyncDataCommand.SyncDataResult res = gson.fromJson(result.get(), SyncDataCommand.SyncDataResult.class);
            if (res != null) {
                return new ThreadCacheObject(res.endpoints(), res.blockedUserIDs(), res.bypassedIPs(), res.routes());
            }
        }
        return null;
    }
}
