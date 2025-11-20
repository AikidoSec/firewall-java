package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttackWave;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.AttackQueue;
import dev.aikido.agent_api.storage.attack_wave_detector.AttackWaveDetectorStore;
import dev.aikido.agent_api.storage.routes.RoutesStore;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;

import static dev.aikido.agent_api.api_discovery.GetApiInfo.getApiInfo;
import static dev.aikido.agent_api.helpers.url.IsUsefulRoute.isUsefulRoute;

public final class WebResponseCollector {
    private WebResponseCollector() {}
    // Only do API Discovery on first 20 requests:
    private static final int ANALYSIS_ON_FIRST_X_REQUESTS = 20;
    /**
     * This function gets used after the code of a request is complete, and we have a status code
     * Here we can check if a route is useful, store it, generate api specs, ...
     * @param statusCode is the response status code
     */
    public static void report(int statusCode) {
        ContextObject context = Context.get();
        if (statusCode <= 0 || context == null) {
            return; // Status code below or equal to zero: Invalid request
        }

        // Check for attack waves (after request is complete and user has been set)
        if (AttackWaveDetectorStore.check(context)) {
            AttackQueue.add(
                DetectedAttackWave.createAPIEvent(context)
            );
            StatisticsStore.incrementAttackWavesDetected();
        }

        RouteMetadata routeMetadata = context.getRouteMetadata();
        if (routeMetadata == null || !isUsefulRoute(statusCode, context.getRoute(), context.getMethod())) {
            return;
        }

        RoutesStore.addRouteHits(routeMetadata);

        // check if we need to generate api spec
        int hits = RoutesStore.getRouteHits(routeMetadata);
        if (hits <= ANALYSIS_ON_FIRST_X_REQUESTS) {
            APISpec apiSpec = getApiInfo(context);
            RoutesStore.updateApiSpec(routeMetadata, apiSpec);
        }
    }
}