package dev.aikido.agent_api.vulnerabilities.shell_injection;

import dev.aikido.agent_api.vulnerabilities.Detector;

import java.util.Map;

import static dev.aikido.agent_api.vulnerabilities.shell_injection.CommandEncapsulationChecker.isSafelyEncapsulated;
import static dev.aikido.agent_api.vulnerabilities.shell_injection.DangerousShellChars.containDangerousCharacter;
import static dev.aikido.agent_api.vulnerabilities.shell_injection.ShellSyntaxChecker.containsShellSyntax;

public class ShellInjectionDetector implements Detector {
    public static final ShellInjectionDetector INSTANCE = new ShellInjectionDetector();

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
        // Check for shell invocation patterns before encapsulation check
        // Commands like "sh -c <user_input>" or "bash -c <user_input>" are dangerous
        // even if the user input appears to be quoted, because the shell will execute it
        if (isShellInvocationWithUserInput(command, userInput)) {
            return getResult(command);
        }
        if (isSafelyEncapsulated(command, userInput)) {
            return new DetectorResult();
        }
        if (containsShellSyntax(command, userInput)) {
            return getResult(command);
        }
        return new DetectorResult();
    }

    private static boolean isShellInvocationWithUserInput(String command, String userInput) {
        // Check if the command invokes a shell with -c flag and the user input is in the argument
        // Patterns: sh -c "...", bash -c "...", /bin/sh -c "...", etc.
        // These are dangerous because the argument to -c is executed as shell code
        
        // Common shell executables
        String[] shells = {"sh", "bash", "zsh", "ksh", "csh", "tcsh", "dash"};
        
        for (String shell : shells) {
            // Look for the shell command at the start or after a path separator
            // Patterns to match:
            // - "sh -c ..." (at start)
            // - "/bin/sh -c ..." (with path)
            // - "/usr/bin/bash -c ..." (with path)
            
            // Check if command starts with the shell name followed by " -c"
            if (command.startsWith(shell + " -c")) {
                String afterCFlag = command.substring((shell + " -c").length()).trim();
                if (afterCFlag.contains(userInput)) {
                    return true;
                }
            }
            
            // Check if command contains the shell with a path prefix
            // Look for patterns like /bin/sh -c, /usr/bin/bash -c, etc.
            // We need to find "/<something>/" + shell + " -c"
            String shellWithFlag = shell + " -c";
            int shellIndex = command.indexOf(shellWithFlag);
            
            while (shellIndex != -1) {
                // Check if this is at the start or if there's a path before it
                if (shellIndex == 0) {
                    // Shell command at the start (already handled above, but keep for completeness)
                    String afterCFlag = command.substring(shellWithFlag.length()).trim();
                    if (afterCFlag.contains(userInput)) {
                        return true;
                    }
                } else {
                    // Check if there's a path before the shell command
                    // Look backwards to see if we have a path like /bin/, /usr/bin/, etc.
                    char charBefore = command.charAt(shellIndex - 1);
                    if (charBefore == '/') {
                        // This looks like a path to the shell executable
                        // e.g., /bin/sh -c, /usr/bin/bash -c
                        String afterCFlag = command.substring(shellIndex + shellWithFlag.length()).trim();
                        if (afterCFlag.contains(userInput)) {
                            return true;
                        }
                    } else if (isSeparatorChar(charBefore)) {
                        // Shell command after a separator (e.g., "ls; sh -c ...")
                        String afterCFlag = command.substring(shellIndex + shellWithFlag.length()).trim();
                        if (afterCFlag.contains(userInput)) {
                            return true;
                        }
                    }
                }
                
                // Look for next occurrence
                shellIndex = command.indexOf(shellWithFlag, shellIndex + 1);
            }
        }
        
        return false;
    }
    
    private static boolean isSeparatorChar(char c) {
        // Characters that separate commands or arguments
        return c == ' ' || c == '\t' || c == '\n' || c == ';' || c == '&' || 
               c == '|' || c == '(' || c == ')' || c == '<' || c == '>' ||
               c == '\r' || c == '\f';
    }

    private static DetectorResult getResult(String command) {
        return new DetectorResult(
            /* detectedAttack: */ true,
            /* metadata: */ Map.of("command", command),
            /* exception: */ ShellInjectionException.get()
        );
    }
}
