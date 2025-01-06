package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.ipc_commands.Command;
import dev.aikido.agent_api.background.ipc_commands.SyncDataCommand;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;

import java.util.Optional;

import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;
import static dev.aikido.agent_api.helpers.BackgroundProcessIdentifier.isBackgroundProcess;

public final class ThreadCacheRenewal {
    private ThreadCacheRenewal() {}
    public static ThreadCacheObject renewThreadCache() {
        // Fetch thread cache over IPC:
        ThreadIPCClient client = getDefaultThreadIPCClient();
        if (client == null || isBackgroundProcess()) {
            return null;
        }
        Optional<SyncDataCommand.Res> result = new SyncDataCommand().send(client, new Command.EmptyResult());
        if(result.isPresent()) {
            SyncDataCommand.Res res = result.get();
            if (res != null) {
                Optional<ReportingApi.APIListsResponse> blockedListsRes = Optional.empty();
                if (res.blockedListsRes() != null) {
                    blockedListsRes = Optional.of(res.blockedListsRes());
                }
                return new ThreadCacheObject(res.endpoints(), res.blockedUserIDs(), res.bypassedIPs(), res.routes(), blockedListsRes);
            }
        }
        return null;
    }
}
