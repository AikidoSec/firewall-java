package dev.aikido.agent_api.vulnerabilities.shell_injection;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static dev.aikido.agent_api.vulnerabilities.shell_injection.DangerousShellChars.containDangerousCharacter;
import static dev.aikido.agent_api.vulnerabilities.shell_injection.ShellCommandsRegex.getCommandsRegex;

public final class ShellSyntaxChecker {
    private ShellSyntaxChecker() {}
    private static final List<String> SEPARATORS = Arrays.asList(
            " ", "\t", "\n", ";", "&", "|", "(", ")", "<", ">", "\r", "\f"
    );

    public static boolean containsShellSyntax(String command, String userInput) {
        if(userInput.isBlank()) {
            return false; // The entire user input is just whitespace, ignore
        }
        if (containDangerousCharacter(userInput)) {
            return true;
        }

        // Check if the command is the same as the user input
        // If user controls the entire command, this is always dangerous
        // even if it's not in our known dangerous commands list
        if (command.equals(userInput)) {
            // First check if it matches a known dangerous command
            Matcher matcher = getCommandsRegex().matcher(command);
            while (matcher.find()) {
                if (matcher.group().equals(command)) {
                    return true;
                }
            }
            
            // Even if not in our list, if the user input equals the entire command
            // and it looks like a command (not just a simple string), block it
            // This is a fail-closed approach for security
            // We allow simple strings but block anything that looks executable
            if (looksLikeCommand(userInput)) {
                return true;
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

    private static boolean looksLikeCommand(String input) {
        // Check if the input looks like it could be a command
        // This is a heuristic to catch cases not in our known commands list
        
        // If it contains path separators, it might be a path to an executable
        if (input.contains("/") || input.contains("\\")) {
            return true;
        }
        
        // If it contains spaces and looks like a command with arguments
        if (input.contains(" ")) {
            String[] parts = input.trim().split("\\s+");
            if (parts.length > 0) {
                String firstPart = parts[0];
                // Check if the first part looks like a command name
                // (alphanumeric, underscore, dash, or path)
                if (firstPart.matches("[a-zA-Z0-9_\\-./\\\\]+")) {
                    return true;
                }
            }
        }
        
        // If it's a single word that could be a command name
        if (input.matches("[a-zA-Z0-9_\\-]+")) {
            // Single word that looks like it could be a command
            // Be conservative here - only block if it really looks like a command
            // Allow simple strings like "test", "data", etc.
            // But block things that are clearly command-like
            
            // If it's very short (1-2 chars), it's probably not a command
            if (input.length() <= 2) {
                return false;
            }
            
            // If it contains common command patterns, block it
            // This is a conservative list to avoid false positives
            return false; // For now, don't block simple words
        }
        
        return false;
    }
}
