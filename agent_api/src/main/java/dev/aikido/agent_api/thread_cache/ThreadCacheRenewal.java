package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.ipc_commands.Command;
import dev.aikido.agent_api.background.ipc_commands.SyncDataCommand;
import dev.aikido.agent_api.background.ipc_commands.UpdateAgentDataCommand;
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
        // Let's get our previous cache object and if necessary sync data :
        // shouldFetch is set as false to ensure we do not enter into infinite recursion.
        ThreadCacheObject cache = ThreadCache.get(/*shouldFetch*/ false);
        if (cache != null) {
            // Send stored data from thread cache to background process: hit counts, middleware data, ... :
            var updateRes = new UpdateAgentDataCommand.Res(
                    /* routeHitDeltas */ cache.getRoutes().getDeltaMap(),
                    /* hitsDelta */ cache.getTotalHits(),
                    /* middlewareInstalled */ cache.isMiddlewareInstalled()
            );
            new UpdateAgentDataCommand().send(client, updateRes);
        }

        // Fetch new data from background process : 
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
