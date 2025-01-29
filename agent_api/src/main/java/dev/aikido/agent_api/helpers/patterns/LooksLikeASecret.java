package dev.aikido.agent_api.helpers.patterns;

import java.util.HashSet;
import java.util.Set;

public final class LooksLikeASecret {
    private LooksLikeASecret() {
    }

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = LOWERCASE.toUpperCase();
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!#$%^&*|;:<>";
    private static final String[] KNOWN_WORD_SEPARATORS = {"-"};
    private static final String WHITE_SPACE = " ";
    private static final int MINIMUM_LENGTH = 10;

    public static boolean looksLikeASecret(String s) {
        if (s.length() <= MINIMUM_LENGTH) {
            return false;
        }

        boolean hasNumber = containsAny(s, NUMBERS);
        if (!hasNumber) {
            return false;
        }

        boolean hasLower = containsAny(s, LOWERCASE);
        boolean hasUpper = containsAny(s, UPPERCASE);
        boolean hasSpecial = containsAny(s, SPECIAL);
        int charsetCount = 0;

        if (hasLower) charsetCount++;
        if (hasUpper) charsetCount++;
        if (hasSpecial) charsetCount++;

        if (charsetCount < 2) {
            return false;
        }

        if (s.contains(WHITE_SPACE)) {
            return false;
        }

        for (String separator : KNOWN_WORD_SEPARATORS) {
            if (s.contains(separator)) {
                return false;
            }
        }

        return averageUniqueCharRatio(s) > 0.75;
    }

    private static boolean containsAny(String s, String characters) {
        for (char c : characters.toCharArray()) {
            if (s.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static double averageUniqueCharRatio(String s) {
        int windowSize = MINIMUM_LENGTH;
        double totalRatio = 0.0;
        int count = 0;

        for (int i = 0; i <= s.length() - windowSize; i++) {
            String window = s.substring(i, i + windowSize);
            Set<Character> uniqueChars = new HashSet<>();
            for (char c : window.toCharArray()) {
                uniqueChars.add(c);
            }
            totalRatio += (double) uniqueChars.size() / windowSize;
            count++;
        }

        return count > 0 ? totalRatio / count : 0.0;
    }
}
