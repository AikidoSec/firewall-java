package helpers;

import dev.aikido.agent_api.helpers.patterns.SafePatternCompiler;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class SafePatternCompilerTest {

    @Test
    public void testCompilePatternSafely_ValidRegex() {
        String regex = "^[a-zA-Z0-9]+$"; // Valid regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNotNull(pattern);
        assertTrue(pattern.matcher("Test123").matches());
        assertFalse(pattern.matcher("Test 123").matches());
    }

    @Test
    public void testCompilePatternSafely_EmptyRegex() {
        String regex = ""; // Empty regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNull(pattern);
    }

    @Test
    public void testCompilePatternSafely_NullRegex() {
        String regex = null; // Null regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNull(pattern);
    }

    @Test
    public void testCompilePatternSafely_InvalidRegex_UnmatchedParentheses() {
        String regex = "(abc"; // Invalid regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNull(pattern);
    }

    @Test
    public void testCompilePatternSafely_InvalidRegex_UnmatchedBrackets() {
        String regex = "[abc"; // Invalid regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNull(pattern);
    }

    @Test
    public void testCompilePatternSafely_InvalidRegex_EmptyCharacterClass() {
        String regex = "[]"; // Invalid regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNull(pattern);
    }

    @Test
    public void testCompilePatternSafely_InvalidRegex_InvalidQuantifier() {
        String regex = "a{2,1}"; // Invalid regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNull(pattern);
    }

    @Test
    public void testCompilePatternSafely_ValidRegex_WithFlags() {
        String regex = "abc"; // Valid regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, Pattern.CASE_INSENSITIVE);
        assertNotNull(pattern);
        assertTrue(pattern.matcher("ABC").matches());
    }

    @Test
    public void testCompilePatternSafely_InvalidRegex_NamedGroup() {
        String regex = "(?<name)"; // Invalid regex
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNull(pattern);
    }

    @Test
    public void testCompilePatternSafely_ValidRegex_WithSpecialCharacters() {
        String regex = "\\d{3}-\\d{2}-\\d{4}"; // Valid regex for SSN format
        Pattern pattern = SafePatternCompiler.compilePatternSafely(regex, 0);
        assertNotNull(pattern);
        assertTrue(pattern.matcher("123-45-6789").matches());
        assertFalse(pattern.matcher("123-456-789").matches());
    }
}
