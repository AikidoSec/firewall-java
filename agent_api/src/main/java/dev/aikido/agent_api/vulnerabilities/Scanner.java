package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.background.ipc_commands.AttackCommand;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;
import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;
import static dev.aikido.agent_api.helpers.StackTrace.getCurrentStackTrace;
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
                    reportAttack(
                        new Attack(operation, vulnerability, source, path, detectorResult.getMetadata(), userInput, getCurrentStackTrace()), ctx
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
    public static void reportAttack(Attack attack, ContextObject ctx) {
        ThreadIPCClient client = getDefaultThreadIPCClient();
        if (client != null) {
            AttackCommand.sendAttack(client, new AttackCommand.Req(attack, ctx));
        }
    }
}
