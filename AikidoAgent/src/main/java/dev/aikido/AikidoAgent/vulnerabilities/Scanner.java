package dev.aikido.AikidoAgent.vulnerabilities;

import dev.aikido.AikidoAgent.background.utilities.IPCClient;
import dev.aikido.AikidoAgent.background.utilities.IPCDefaultClient;
import dev.aikido.AikidoAgent.context.Context;
import dev.aikido.AikidoAgent.context.ContextObject;
import jnr.ffi.annotations.In;

import java.util.Map;

public class Scanner {
    public static void run(Attacks.Attack attack, String operation, String[] arguments) {
        ContextObject ctx = Context.get();
        if (ctx == null) { // Client is never null
            return;
        }
        Injection injection = null;
        try {
            Map<String, Map<String, String>> stringsFromContext = new StringsFromContext(ctx).getAll();
            for (Map.Entry<String, Map<String, String>> sourceEntry : stringsFromContext.entrySet()) {
                String source = sourceEntry.getKey();
                for (Map.Entry<String, String> entry : sourceEntry.getValue().entrySet()) {
                    // Extract values :
                    String userInput = entry.getKey();
                    String path = entry.getValue();
                    // Run Injection code :
                    boolean isInjection = attack.getDetector().run(userInput, arguments);
                    if (isInjection) {
                        System.out.println("Detected an injection: user input : " + userInput + ", Path " + path);
                        Map<String, String> metadata = Map.of("sql", arguments[0]); // Fix
                        injection = new Injection(operation, attack, source, path, metadata, userInput);
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace(); // Temporary logging measure
        }
        if (injection != null) {
            // Report to background :
            IPCClient client = new IPCDefaultClient();
            injection.reportOverIPC(client);
            // Throw error :
            throw new RuntimeException(injection.kind);
        }
    }
}
