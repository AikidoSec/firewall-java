package dev.aikido.agent_api.vulnerabilities.shell_injection;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static dev.aikido.agent_api.vulnerabilities.shell_injection.DangerousShellChars.containDangerousCharacter;
import static dev.aikido.agent_api.vulnerabilities.shell_injection.ShellCommandsRegex.getCommandsRegex;

public final class ShellSyntaxChecker {
    private ShellSyntaxChecker() {}
    private static final List<String> SEPARATORS = Arrays.asList(
            " ", "\t", "\n", ";", "&", "|", "(", ")", "<", ">"
    );

    public static boolean containsShellSyntax(String command, String userInput) {
        if(userInput.isBlank()) {
            return false; // The entire user input is just whitespace, ignore
        }
        if (containDangerousCharacter(userInput)) {
            return true;
        }

        // Check if the command is the same as the user input
        // Rare case, but it's possible
        // e.g. command is `shutdown` and user input is `shutdown`
        // (`shutdown -h now` will be caught by the dangerous chars as it contains a space)
        if (command.equals(userInput)) {
            Matcher matcher = getCommandsRegex().matcher(command);
            while (matcher.find()) {
                if (matcher.group().equals(command)) {
                    return true;
                }
            }
            return false;
        }

        // Check if the command contains a commonly used command
        Matcher matcher = getCommandsRegex().matcher(command);
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
