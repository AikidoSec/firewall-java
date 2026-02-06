package dev.aikido.agent_api.vulnerabilities.shell_injection;

import java.util.Arrays;
import java.util.List;

public final class DangerousShellChars {
    private DangerousShellChars() {}
    private static final List<String> DANGEROUS_CHARS = Arrays.asList(
            "#", "!", "\"", "$", "&", "'", "(", ")", "*", ";", "<", "=", ">", "?",
            "[", "\\", "]", "^", "`", "{", "|", "}", " ", "\n", "\t", "~", "\r", "\f"
    );

    public static boolean containDangerousCharacter(String userInput) {
        return DANGEROUS_CHARS.stream().anyMatch(userInput::contains);
    }
}
