package dev.aikido.agent_api.vulnerabilities.shell_injection;

import dev.aikido.agent_api.vulnerabilities.Detector;

import java.util.Map;

import static dev.aikido.agent_api.vulnerabilities.shell_injection.CommandEncapsulationChecker.isSafelyEncapsulated;
import static dev.aikido.agent_api.vulnerabilities.shell_injection.DangerousShellChars.containDangerousCharacter;
import static dev.aikido.agent_api.vulnerabilities.shell_injection.ShellSyntaxChecker.containsShellSyntax;

public class ShellInjectionDetector implements Detector {
    @Override
    public DetectorResult run(String userInput, String[] arguments) {
        if (userInput.isEmpty() || arguments == null || arguments.length == 0 || arguments[0] == null) {
           return new DetectorResult(); // Empty result.
        }
        String command = arguments[0];
        if (userInput.equals("~") && command.length() > 1 && command.contains("~")) {
            // Block single ~ character. E.g.: "echo ~"
            return getResult(command);
        }
        if (userInput.length() == 1) {
            // We ignore single characters since they don't pose a big threat.
            // They are only able to crash the shell, not execute arbitrary commands.
            return new DetectorResult();
        }
        if (userInput.length() > command.length()) {
            // We ignore cases where the user input is longer than the command.
            // Because the user input can't be part of the command.
            return new DetectorResult();
        }
        if (!command.contains(userInput)) {
            return new DetectorResult();
        }
        if (isSafelyEncapsulated(command, userInput)) {
            return new DetectorResult();
        }
        if (containsShellSyntax(command, userInput)) {
            return getResult(command);
        }
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
