package dev.aikido.agent_api.vulnerabilities.shell_injection;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static dev.aikido.agent_api.vulnerabilities.shell_injection.DangerousShellChars.containDangerousCharacter;
import static dev.aikido.agent_api.vulnerabilities.shell_injection.ShellCommandsRegex.COMMANDS_REGEX;

public class ShellSyntaxChecker {
    private static final List<String> SEPARATORS = Arrays.asList(
            " ", "\t", "\n", ";", "&", "|", "(", ")", "<", ">"
    );

    public static boolean containsShellSyntax(String userInput, String command) {
        if (containDangerousCharacter(userInput)) {
            return true;
        }

        if (command.equals(userInput)) {
            Matcher match = COMMANDS_REGEX.matcher(command);
            // Check if there is a match on the command regex that spans full command:
            return match.find() && match.start() == 0 && match.end() == command.length();
        }
        // Check if the command contains a commonly used command
        Matcher matcher = COMMANDS_REGEX.matcher(command);
        while (matcher.find()) {
            // We found a command like `rm` or `/sbin/shutdown` in the command
            // Check if the command is the same as the user input
            if (!userInput.equals(matcher.group())) {
                continue;
            }

            // Check surrounding characters
            char charBefore = (matcher.start() > 0) ? command.charAt(matcher.start() - 1) : '\0';
            char charAfter = (matcher.end() < command.length()) ? command.charAt(matcher.end()) : '\0';

            // Check surrounding characters
            if (SEPARATORS.contains(String.valueOf(charBefore)) && SEPARATORS.contains(String.valueOf(charAfter))) {
                return true; // e.g. `<separator>rm<separator>`
            }
            if (SEPARATORS.contains(String.valueOf(charBefore)) && charAfter == '\0') {
                return true; // e.g. `<separator>rm`
            }
            if (charBefore == '\0' && SEPARATORS.contains(String.valueOf(charAfter))) {
                return true; // e.g. `rm<separator>`
            }
        }
        return false;
    }
}
