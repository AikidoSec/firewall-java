package dev.aikido.agent_api.vulnerabilities;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.utilities.IPCClient;
import dev.aikido.agent_api.background.utilities.IPCDefaultClient;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;
import static dev.aikido.agent_api.helpers.StackTrace.getCurrentStackTrace;

public class Scanner {
    private static final Logger logger = LogManager.getLogger(Scanner.class);
    private record AttackCommandData(Attack attack, ContextObject context) {}
    public static void scanForGivenVulnerability(Vulnerabilities.Vulnerability vulnerability, String operation, String[] arguments) {
        ContextObject ctx = Context.get();
        if (ctx == null) { // Client is never null
            return;
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
                    Detector.DetectorResult detectorResult = vulnerability.getDetector().run(userInput, arguments);
                    if (!detectorResult.isDetectedAttack()) {
                        continue;
                    }
                    exception = Optional.of(detectorResult.getException());
                    // Report attack :
                    Attack attack = new Attack(operation, vulnerability, source, path, detectorResult.getMetadata(), userInput, getCurrentStackTrace());
                    Gson gson = new Gson();
                    String json = gson.toJson(new AttackCommandData(attack, ctx));

                    IPCClient client = new IPCDefaultClient();
                    client.sendData(
                            "ATTACK$" + json, // data
                            false // receive
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
