package dev.aikido.AikidoAgent.vulnerabilities;

import dev.aikido.AikidoAgent.context.Context;
import dev.aikido.AikidoAgent.context.ContextObject;

public class Scanner {
    public static void run(Attacks.Attack attack, String operation, String[] arguments) {
        ContextObject ctx = Context.get();
        String[] userInputs = {}; // Update in later PR
        boolean isInjection = false;
        try {
            for (String userInput : userInputs) {
                if (attack.getDetector().run(userInput, arguments)) {
                    // Attack detected, set injection and break for loop.
                    isInjection = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Temporary logging measure
        }
        if (isInjection) {
            // Report to background :

            // Throw error :
            throw new RuntimeException("SQL Injection");
        }
    }
}
