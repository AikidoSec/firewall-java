package dev.aikido.agent_api.vulnerabilities.attack_wave_detection;

import java.util.List;

public final class MethodChecker {
    private MethodChecker() {
    }

    private static final List<String> WEB_SCAN_METHODS = List.of(
        "BADMETHOD",
        "BADHTTPMETHOD",
        "BADDATA",
        "BADMTHD",
        "BDMTHD"
    );

    public static boolean isWebScanMethod(String method) {
        return WEB_SCAN_METHODS.contains(method.toUpperCase());
    }
}
