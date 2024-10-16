package dev.aikido.AikidoAgent.vulnerabilities;

import com.google.gson.Gson;
import dev.aikido.AikidoAgent.background.utilities.IPCClient;
import dev.aikido.AikidoAgent.background.utilities.IPCDefaultClient;
import dev.aikido.AikidoAgent.context.Context;
import dev.aikido.AikidoAgent.context.ContextObject;

import java.util.Map;

public class Scanner {
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
                        System.out.println("Detected an injection: user input : " + userInput + ", Path " + path);
                        Map<String, String> metadata = Map.of("sql", arguments[0]); // Fix
                        attack = new Attack(operation, vulnerability, source, path, metadata, userInput);
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace(); // Temporary logging measure
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
