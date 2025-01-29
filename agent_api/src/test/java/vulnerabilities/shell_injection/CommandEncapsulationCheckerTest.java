package vulnerabilities.shell_injection;

import static dev.aikido.agent_api.vulnerabilities.shell_injection.CommandEncapsulationChecker.isSafelyEncapsulated;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandEncapsulationCheckerTest {

    @Test
    public void testSafeBetweenSingleQuotes() {
        assertTrue(isSafelyEncapsulated("echo '$USER'", "$USER"));
        assertTrue(isSafelyEncapsulated("echo '`$USER'", "`USER"));
    }

    @Test
    public void testSingleQuoteInSingleQuotes() {
        assertFalse(isSafelyEncapsulated("echo ''USER'", "'USER"));
    }

    @Test
    public void testDangerousCharsBetweenDoubleQuotes() {
        assertTrue(isSafelyEncapsulated("echo \"=USER\"", "=USER"));

        assertFalse(isSafelyEncapsulated("echo \"$USER\"", "$USER"));
        assertFalse(isSafelyEncapsulated("echo \"!USER\"", "!USER"));
        assertFalse(isSafelyEncapsulated("echo \"\\`USER\"", "`USER"));
        assertFalse(isSafelyEncapsulated("echo \"\\USER\"", "\\USER"));
    }

    @Test
    public void testSameUserInputMultipleTimes() {
        assertTrue(isSafelyEncapsulated("echo '$USER' '$USER'", "$USER"));
        assertFalse(isSafelyEncapsulated("echo \"$USER\" '$USER'", "$USER"));
        assertFalse(isSafelyEncapsulated("echo \"$USER\" \"$USER\"", "$USER"));
    }

    @Test
    public void testFirstAndLastQuoteDoesNotMatch() {
        assertFalse(isSafelyEncapsulated("echo '$USER\"", "$USER"));
        assertFalse(isSafelyEncapsulated("echo \"$USER'", "$USER"));
    }

    @Test
    public void testFirstOrLastCharacterNotEscapeChar() {
        assertFalse(isSafelyEncapsulated("echo $USER'", "$USER"));
        assertFalse(isSafelyEncapsulated("echo $USER\"", "$USER"));
    }

    @Test
    public void testUserInputDoesNotOccurInCommand() {
        assertTrue(isSafelyEncapsulated("echo 'USER'", "$USER"));
        assertTrue(isSafelyEncapsulated("echo \"USER\"", "$USER"));
    }
}
