package dev.aikido.agent_api.helpers.patterns;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class SafePatternCompiler {
    private SafePatternCompiler() {
    }

    public static Pattern compilePatternSafely(String regex, int flags) {
        if (regex == null || regex.isEmpty()) {
            return null;
        }
        try {
            return Pattern.compile(regex, flags);
        } catch (PatternSyntaxException ignored) {
            return null;
        }
    }
}
