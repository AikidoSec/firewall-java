package dev.aikido.agent_api.vulnerabilities.shell_injection;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class CommandEncapsulationChecker {
    private CommandEncapsulationChecker() {}
    private static final List<Character> ESCAPE_CHARS = Arrays.asList('"', '\'');
    private static final List<Character> DANGEROUS_CHARS_INSIDE_DOUBLE_QUOTES = Arrays.asList('$', '`', '\\', '!');

    public static boolean isSafelyEncapsulated(String command, String userInput) {
        // Split the command by the user input
        String[] segments = command.split(Pattern.quote(userInput), -1);

        for (int i = 0; i < segments.length - 1; i++) {
            String currentSegment = segments[i];
            String nextSegment = segments[i + 1];

            Character charBeforeUserInput = currentSegment.isEmpty() ? null : currentSegment.charAt(currentSegment.length() - 1);
            Character charAfterUserInput = nextSegment.isEmpty() ? null : nextSegment.charAt(0);

            boolean isEscapeChar = charBeforeUserInput != null && ESCAPE_CHARS.contains(charBeforeUserInput);

            if (!isEscapeChar) {
                return false;
            }

            if (!charBeforeUserInput.equals(charAfterUserInput)) {
                return false;
            }

            if (userInput.indexOf(charBeforeUserInput) >= 0) {
                return false;
            }

            if (charBeforeUserInput.equals('"')) {
                /*
                    There are no dangerous characters inside single quotes
                    You can use certain characters inside double quotes
                    https://www.gnu.org/software/bash/manual/html_node/Single-Quotes.html
                    https://www.gnu.org/software/bash/manual/html_node/Double-Quotes.html
                */
                for (Character dangerousChar : DANGEROUS_CHARS_INSIDE_DOUBLE_QUOTES) {
                    if (userInput.contains(dangerousChar.toString())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
