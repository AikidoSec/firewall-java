package dev.aikido.agent_api.vulnerabilities.shell_injection;

import dev.aikido.agent_api.vulnerabilities.Detector;

import java.util.Map;

import static dev.aikido.agent_api.vulnerabilities.shell_injection.DangerousShellChars.containDangerousCharacter;

public class ShellInjectionDetector implements Detector {
    @Override
    public DetectorResult run(String userInput, String[] arguments) {
        if (userInput.isEmpty() || arguments == null || arguments.length == 0) {
           return new DetectorResult(); // Empty result.
        }
        String command = arguments[0];
        return new DetectorResult();
    }

    private static DetectorResult getResult(String command) {
        return new DetectorResult(
            /* detectedAttack: */ true,
            /* metadata: */ Map.of("command", command),
            /* exception: */ ShellInjectionException.get()
        );
    }
}
