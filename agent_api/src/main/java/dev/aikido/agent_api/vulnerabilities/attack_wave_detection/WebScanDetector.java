package dev.aikido.agent_api.vulnerabilities.attack_wave_detection;

import dev.aikido.agent_api.context.ContextObject;

import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.MethodChecker.isWebScanMethod;
import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.WebQueryParamChecker.queryParamsContainDangerousPayload;
import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.PathChecker.isWebScanPath;

public final class WebScanDetector {
    private WebScanDetector() {}

    public static boolean isWebScanner(ContextObject ctx) {
        String method = ctx.getMethod();
        if (method != null && isWebScanMethod(method)) {
            return true;
        }

        String route = ctx.getRoute();
        if (route != null && isWebScanPath(route)) {
            return true;
        }

        if (queryParamsContainDangerousPayload(ctx.getQuery())) {
            return true;
        }

        return false;
    }
}
