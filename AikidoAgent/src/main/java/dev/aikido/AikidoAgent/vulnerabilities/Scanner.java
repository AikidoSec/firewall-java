package dev.aikido.AikidoAgent.vulnerabilities;

public class Scanner {
    public static void run(Attacks.Attack attack, String operation, String[] arguments) {
        String userInput = "banana";
        boolean isInjection = attack.getDetector().run(userInput, arguments);
        if (isInjection) {
            // Report to background :
            
            // Throw error
            throw new RuntimeException("SQL Injection");
        }
    }
}
