package vulnerabilities.shell_injection;


import dev.aikido.agent_api.vulnerabilities.shell_injection.ShellSyntaxChecker;
import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.vulnerabilities.shell_injection.ShellSyntaxChecker.containsShellSyntax;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShellSyntaxCheckerTest {

    @Test
    public void testDetectsShellSyntax() {
        assertFalse(containsShellSyntax("", ""));
        assertFalse(containsShellSyntax("hello", "hello"));
        assertFalse(containsShellSyntax("\n", "\n"));
        assertFalse(containsShellSyntax("\n\n", "\n\n"));

        assertTrue(containsShellSyntax("$(command)", "$(command)"));
        assertTrue(containsShellSyntax("$(command arg arg)", "$(command arg arg)"));
        assertTrue(containsShellSyntax("`command`", "`command`"));
        assertTrue(containsShellSyntax("\narg", "\narg"));
        assertTrue(containsShellSyntax("\targ", "\targ"));
        assertTrue(containsShellSyntax("\narg\n", "\narg\n"));
        assertTrue(containsShellSyntax("arg\n", "arg\n"));
        assertTrue(containsShellSyntax("arg\narg", "arg\narg"));
        assertTrue(containsShellSyntax("rm -rf", "rm -rf"));
        assertTrue(containsShellSyntax("/bin/rm -rf", "/bin/rm -rf"));
        assertTrue(containsShellSyntax("/bin/rm", "/bin/rm"));
        assertTrue(containsShellSyntax("/sbin/sleep", "/sbin/sleep"));
        assertTrue(containsShellSyntax("/usr/bin/kill", "/usr/bin/kill"));
        assertTrue(containsShellSyntax("/usr/bin/killall", "/usr/bin/killall"));
        assertTrue(containsShellSyntax("/usr/bin/env", "/usr/bin/env"));
        assertTrue(containsShellSyntax("/bin/ps", "/bin/ps"));
        assertTrue(containsShellSyntax("/usr/bin/W", "/usr/bin/W"));
        assertTrue(containsShellSyntax("lsattr", "lsattr"));
    }

    @Test
    public void testDetectsCommandsSurroundedBySeparators() {
        assertTrue(containsShellSyntax(
                "find /path/to/search -type f -name \"pattern\" -exec rm {} \\;", "rm"
        ));
    }

    @Test
    public void testDetectsCommandsWithSeparatorBefore() {
        assertTrue(containsShellSyntax(
                "find /path/to/search -type f -name \"pattern\" | xargs rm", "rm"
        ));
    }

    @Test
    public void testDetectsCommandsWithSeparatorAfter() {
        assertTrue(containsShellSyntax("rm arg", "rm"));
    }

    @Test
    public void testChecksIfSameCommandOccursInUserInput() {
        assertFalse(containsShellSyntax("find cp", "rm"));
    }

    @Test
    public void testTreatsColonAsCommand() {
        assertTrue(containsShellSyntax(":|echo", ":|"));
        assertFalse(containsShellSyntax("https://www.google.com", "https://www.google.com"));
    }

    @Test
    public void testDetectsCommandsWithSeparators() {
        assertTrue(containsShellSyntax("rm>echo", "echo"));
        assertTrue(containsShellSyntax("rm>arg", "rm"));
        assertTrue(containsShellSyntax("rm<arg", "rm"));
    }

    @Test
    public void testDoesNotDetectsCommandsWithSeparators() {
        // Check that if there is no \0 we don't flag it
        assertFalse(containsShellSyntax("rm>echo+", "echo"));
        assertFalse(containsShellSyntax("+rm>arg", "rm"));
        assertFalse(containsShellSyntax("+rm<arg", "rm"));
    }

    @Test
    public void testEmptyCommandAndInput() {
        assertFalse(containsShellSyntax("", ""));
        assertFalse(containsShellSyntax("", "rm"));
        assertFalse(containsShellSyntax("rm", ""));
    }

    @Test
    public void testCommandWithSpecialCharacters() {
        assertTrue(containsShellSyntax("echo $HOME", "echo"));
        assertTrue(containsShellSyntax("echo $HOME", "$HOME"));
        assertTrue(containsShellSyntax("echo \"Hello World\"", "echo"));
        assertTrue(containsShellSyntax("echo 'Hello World'", "echo"));
    }

    @Test
    public void testCommandWithMultipleSeparators() {
        assertTrue(containsShellSyntax("rm -rf; echo 'done'", "rm"));
        assertTrue(containsShellSyntax("ls | grep 'test'", "ls"));
        assertTrue(containsShellSyntax("find . -name '*.txt' | xargs rm", "rm"));
    }

    @Test
    public void testCommandWithPathPrefixes() {
        assertTrue(containsShellSyntax("/bin/rm -rf /tmp", "/bin/rm"));
        assertTrue(containsShellSyntax("/usr/bin/killall process_name", "/usr/bin/killall"));
        assertTrue(containsShellSyntax("/sbin/shutdown now", "/sbin/shutdown"));
    }

    @Test
    public void testCommandWithColon() {
        assertTrue(containsShellSyntax(":; echo 'test'", ":"));
        assertTrue(containsShellSyntax("echo :; echo 'test'", ":"));
    }

    @Test
    public void testCommandWithNewlineSeparators() {
        assertTrue(containsShellSyntax("echo 'Hello'\nrm -rf /tmp", "rm"));
        assertTrue(containsShellSyntax("echo 'Hello'\n", "echo"));
    }

    @Test
    public void testCommandWithTabs() {
        assertTrue(containsShellSyntax("echo 'Hello'\trm -rf /tmp", "rm"));
        assertTrue(containsShellSyntax("\techo 'Hello'", "echo"));
    }

    @Test
    public void testCommandWithInvalidInput() {
        assertFalse(containsShellSyntax("echo 'Hello'", "invalid_command"));
        assertFalse(containsShellSyntax("ls -l", "rm"));
    }

    @Test
    public void testCommandWithMultipleCommands() {
        assertTrue(containsShellSyntax("rm -rf; ls -l; echo 'done'", "ls"));
        assertTrue(containsShellSyntax("echo 'Hello'; rm -rf /tmp", "rm"));
    }

    @Test
    public void testCommandWithNoSeparators() {
        assertFalse(containsShellSyntax("echoHello", "echo"));
        assertFalse(containsShellSyntax("rmrf", "rm"));
    }

    @Test
    public void testCommandWithDangerousChars() {
        assertTrue(containsShellSyntax("rm -rf; echo 'done'", ";"));
        assertTrue(containsShellSyntax("echo 'Hello' & rm -rf /tmp", "&"));
        assertTrue(containsShellSyntax("echo 'Hello' | rm -rf /tmp", "|"));
    }

    @Test
    public void testCommandWithPathAndArguments() {
        assertTrue(containsShellSyntax("/usr/bin/ls -l", "/usr/bin/ls"));
        assertTrue(containsShellSyntax("/bin/cp file1 file2", "/bin/cp"));
    }
}