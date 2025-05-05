package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.EnhancedStackTrace;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.Map;
import java.util.Optional;

import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;
import static dev.aikido.agent_api.storage.AttackQueue.attackDetected;
import static dev.aikido.agent_api.vulnerabilities.SkipVulnerabilityScanDecider.shouldSkipVulnerabilityScan;

public final class Scanner {
    private Scanner() {}
    private static final Logger logger = LogManager.getLogger(Scanner.class);
    public static void scanForGivenVulnerability(Vulnerabilities.Vulnerability vulnerability, String operation, String[] arguments) {
        Detector detector = vulnerability.getDetector();
        if (detector.returnEarly(arguments)) {
            return; // If input is in no way dangerous, do not loop oer user input
        }
        ContextObject ctx = Context.get();
        if (shouldSkipVulnerabilityScan(ctx)) {
            return; // Bypassed IPs, protection forced off, ...
        }
        Optional<AikidoException> exception = Optional.empty();
        try {
            Map<String, Map<String, String>> stringsFromContext = new StringsFromContext(ctx).getAll();
            for (Map.Entry<String, Map<String, String>> sourceEntry : stringsFromContext.entrySet()) {
                String source = sourceEntry.getKey();
                for (Map.Entry<String, String> entry : sourceEntry.getValue().entrySet()) {
                    // Extract values :
                    String userInput = entry.getKey();
                    String path = entry.getValue();
                    // Run attack code :
                    Detector.DetectorResult detectorResult = detector.run(userInput, arguments);
                    if (!detectorResult.isDetectedAttack()) {
                        continue;
                    }
                    exception = Optional.of(detectorResult.getException());
                    // Report attack :
                    attackDetected(
                        new Attack(
                                operation, vulnerability, source,
                                path, detectorResult.getMetadata(), userInput,
                                new EnhancedStackTrace(), ctx.getUser()), ctx
                    );
                    break;
                }
            }
        } catch (Throwable e) {
            logger.debug(e);
        }
        // Run throw code here so it does not get caught :
        if (exception.isPresent() && shouldBlock()) {
            throw exception.get();
        }
    }
}
