package dev.aikido.AikidoAgent.vulnerabilities;

import com.google.gson.Gson;
import dev.aikido.AikidoAgent.background.utilities.IPCClient;
import dev.aikido.AikidoAgent.background.utilities.IPCDefaultClient;
import dev.aikido.AikidoAgent.context.Context;
import dev.aikido.AikidoAgent.context.ContextObject;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Scanner {
    private static final Logger logger = LogManager.getLogger(Scanner.class);
    private record AttackCommandData(Attack attack, ContextObject context) {}
    public static void scanForGivenVulnerability(Vulnerabilities.Vulnerability vulnerability, String operation, String[] arguments) {
        ContextObject ctx = Context.get();
        if (ctx == null) { // Client is never null
            return;
        }
        Attack attack = null;
        try {
            Map<String, Map<String, String>> stringsFromContext = new StringsFromContext(ctx).getAll();
            for (Map.Entry<String, Map<String, String>> sourceEntry : stringsFromContext.entrySet()) {
                String source = sourceEntry.getKey();
                for (Map.Entry<String, String> entry : sourceEntry.getValue().entrySet()) {
                    // Extract values :
                    String userInput = entry.getKey();
                    String path = entry.getValue();
                    // Run attack code :
                    boolean isAttack = vulnerability.getDetector().run(userInput, arguments);
                    if (isAttack) {
                        Map<String, String> metadata = Map.of("sql", arguments[0]); // Fix
                        attack = new Attack(operation, vulnerability, source, path, metadata, userInput);
                        logger.info("Detected {} attack due to input: {}", vulnerability.getKind(), userInput);
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            logger.debug(e);
        }
        if (attack != null) {
            // Report to background :
            IPCClient client = new IPCDefaultClient();
            Gson gson = new Gson();

            String json = gson.toJson(new AttackCommandData(attack, ctx));
            client.sendData("ATTACK$" + json);
            // Throw error :
            throw new RuntimeException(attack.kind);
        }
    }
}
