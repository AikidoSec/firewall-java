package vulnerabilities.shell_injection;

import dev.aikido.agent_api.vulnerabilities.shell_injection.ShellInjectionDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShellInjectionDetectorTest {

    private void assertIsShellInjection(String command, String userInput) {
        assertTrue(new ShellInjectionDetector().run(userInput, new String[]{command}).isDetectedAttack(),
                String.format("command: %s, userInput: %s", command, userInput));
    }

    private void assertIsNotShellInjection(String command, String userInput) {
        assertFalse(new ShellInjectionDetector().run(userInput, new String[]{command}).isDetectedAttack(),
                String.format("command: %s, userInput: %s", command, userInput));
    }

    @Test
    void testSingleCharactersIgnored() {
        assertIsNotShellInjection("ls `", "`");
        assertIsNotShellInjection("ls *", "*");
        assertIsNotShellInjection("ls a", "a");
    }

    @Test
    void testNoUserInput() {
        assertIsNotShellInjection("ls", "");
        assertIsNotShellInjection("ls", " ");
        assertIsNotShellInjection("ls", "  ");
        assertIsNotShellInjection("ls", "   ");
    }

    @Test
    void testUserInputNotInCommand() {
        assertIsNotShellInjection("ls", "$(echo)");
    }

    @Test
    void testUserInputLongerThanCommand() {
        assertIsNotShellInjection("`ls`", "`ls` `ls`");
    }

    @Test
    void testDetectsCommandSubstitution() {
        assertIsShellInjection("ls $(echo)", "$(echo)");
        assertIsShellInjection("ls \"$(echo)\"", "$(echo)");
        assertIsShellInjection("echo $(echo \"Inner: $(echo \"This is nested\")\")",
                "$(echo \"Inner: $(echo \"This is nested\")\")");

        assertIsNotShellInjection("ls '$(echo)'", "$(echo)");
        assertIsNotShellInjection("ls '$(echo \"Inner: $(echo \"This is nested\")\")'",
                "$(echo \"Inner: $(echo \"This is nested\")\")");
    }

    @Test
    void testDetectsBackticks() {
        assertIsShellInjection("echo `echo`", "`echo`");
    }

    @Test
    void testChecksUnsafelyQuoted() {
        assertIsShellInjection("ls '$(echo)", "$(echo)");
    }

    @Test
    void testSingleQuoteBetweenSingleQuotes() {
        assertIsShellInjection("ls ''single quote''", "'single quote'");
    }

    @Test
    void testNoSpecialCharsInsideDoubleQuotes() {
        assertIsShellInjection("ls \"whatever$\"", "whatever$");
        assertIsShellInjection("ls \"whatever!\"", "whatever!");
        assertIsShellInjection("ls \"whatever`\"", "whatever`");
    }

    @Test
    void testNoSemiColon() {
        assertIsShellInjection("ls whatever;", "whatever;");
        assertIsNotShellInjection("ls \"whatever;\"", "whatever;");
        assertIsNotShellInjection("ls 'whatever;'", "whatever;");
    }

    @Test
    void testRmRfExecutedByUsingSemiColon() {
        assertIsShellInjection("ls; rm -rf", "; rm -rf");
    }

    @Test
    void testRmRfIsFlaggedAsShellInjection() {
        assertIsShellInjection("rm -rf", "rm -rf");
    }

    @Test
    void testShellInjectionWithChainedCommandsUsingAnd() {
        assertIsShellInjection("ls && rm -rf /", "&& rm -rf /");
    }

    @Test
    void testShellInjectionWithOrLogic() {
        assertIsShellInjection("ls || echo 'malicious code'", "|| echo 'malicious code'");
    }

    @Test
    void testDetectsRedirectionAttempts() {
        assertIsShellInjection("ls > /dev/null", "> /dev/null");
        assertIsShellInjection("cat file.txt > /etc/passwd", "> /etc/passwd");
    }

    @Test
    void testDetectsAppendRedirectionAttempts() {
        assertIsShellInjection("echo 'data' >> /etc/passwd", ">> /etc/passwd");
    }

    @Test
    void testDetectsPipeCharacterAsShellInjection() {
        assertIsShellInjection("cat file.txt | grep 'password'", "| grep 'password'");
    }

    @Test
    void testAllowsSafeUseOfPipeCharacterWithinQuotes() {
        assertIsNotShellInjection("echo '|'", "|");
    }

    @Test
    void testDetectsNestedCommandSubstitution() {
        assertIsShellInjection("echo $(cat $(ls))", "$(cat $(ls))");
    }

    @Test
    void testAllowsSafeCommandsWithinSingleQuotes() {
        assertIsNotShellInjection("echo 'safe command'", "safe command");
    }

    @Test
    void testDetectsUnsafeUseOfVariables() {
        assertIsShellInjection("echo $USER", "$USER");
        assertIsShellInjection("echo ${USER}", "${USER}");
        assertIsShellInjection("echo \"${USER}\"", "${USER}");
    }

    @Test
    void testAllowsSafeUseOfVariablesWithinQuotes() {
        assertIsNotShellInjection("echo '$USER'", "$USER");
    }

    @Test
    void testDetectsSubshellExecutionWithBackticksInsideDoubleQuotes() {
        assertIsShellInjection("ls \"$(echo `whoami`)", "`whoami`");
    }

    @Test
    void testDetectsCodeInjectionWithNewlineCharacters() {
        assertIsShellInjection("echo 'safe'\necho 'malicious'", "\necho 'malicious'");
    }

    @Test
    void testDetectsAttemptsToEscapeOutOfQuotes() {
        assertIsShellInjection("echo \"safe\"; echo \"malicious\"", "\"; echo \"malicious\"");
    }

    @Test
    void testCorrectlyHandlesWhitespaceInInputs() {
        assertIsNotShellInjection("ls", "   ");
        assertIsShellInjection("ls ; rm -rf /", "; rm -rf /");
    }

    @Test
    void testDetectsFileManipulationCommands() {
        assertIsShellInjection("touch /tmp/malicious", "touch /tmp/malicious");
        assertIsShellInjection("mv /tmp/safe /tmp/malicious", "mv /tmp/safe /tmp/malicious");
    }

    @Test
    void testAllowsCommandsWithConstantsThatResembleUserInput() {
        assertIsNotShellInjection("echo 'userInput'", "userInput");
    }

    @Test
    void testRecognizesSafePathsThatIncludePatternsSimilarToUserInput() {
        assertIsNotShellInjection("ls /constant/path/without/user/input/", "/constant/path/without/user/input/");
    }

    @Test
    void testAcknowledgesSafeUseOfSpecialCharactersWhenProperlyEncapsulated() {
        assertIsNotShellInjection("echo ';'", ";");
        assertIsNotShellInjection("echo '&&'", "&&");
        assertIsNotShellInjection("echo '||'", "||");
    }

    @Test
    void testTreatsEncapsulatedRedirectionAndPipeSymbolsAsSafe() {
        assertIsNotShellInjection("echo 'data > file.txt'", "data > file.txt");
        assertIsNotShellInjection("echo 'find | grep'", "find | grep");
    }

    @Test
    void testRecognizesSafeInclusionOfSpecialPatternsWithinQuotesAsNonInjections() {
        assertIsNotShellInjection("echo '$(command)'", "$(command)");
    }

    @Test
    void testConsidersConstantsWithSemicolonsAsSafeWhenNonExecutable() {
        assertIsNotShellInjection("echo 'text; more text'", "text; more text");
    }

    @Test
    void testAcknowledgesCommandsThatLookDangerousButAreSafeDueToQuoting() {
        assertIsNotShellInjection("echo '; rm -rf /'", "; rm -rf /");
        assertIsNotShellInjection("echo '&& echo malicious'", "&& echo malicious");
    }

    @Test
    void testRecognizesCommandsWithNewlineCharactersAsSafeWhenEncapsulated() {
        assertIsNotShellInjection("echo 'line1\nline2'", "line1\nline2");
    }

    @Test
    void testAcceptsSpecialCharactersInConstantsAsSafeWhenNoExecution() {
        assertIsNotShellInjection("echo '*'", "*");
        assertIsNotShellInjection("echo '?'", "?");
        assertIsNotShellInjection("echo '\\' ", "\\");
    }

    @Test
    void testDoesNotFlagCommandWithMatchingWhitespaceAsInjection() {
        assertIsNotShellInjection("ls -l", " "); // A single space is just an argument separator, not user input
    }

    @Test
    void testIgnoresCommandsWhereMultipleSpacesMatchUserInput() {
        assertIsNotShellInjection("ls   -l", "   "); // Multiple spaces between arguments should not be considered injection
    }

    @Test
    void testDoesNotConsiderLeadingWhitespaceInCommandsAsUserInput() {
        assertIsNotShellInjection("  ls -l", "  "); // Leading spaces before the command are not user-controlled
    }

    @Test
    void testTreatsTrailingWhitespaceInCommandsAsNonInjection() {
        assertIsNotShellInjection("ls -l ", " "); // Trailing space after the command is benign
    }

    @Test
    void testRecognizesSpacesBetweenQuotesAsNonInjective() {
        assertIsNotShellInjection("echo ' ' ", " "); // Space within quotes is part of the argument, not a separator
    }

    @Test
    void testHandlesSpacesWithinQuotedArgumentsCorrectly() {
        assertIsNotShellInjection("command 'arg with spaces'", " "); // Spaces within a quoted argument should not be flagged
    }

    @Test
    void testCorrectlyInterpretsSpacesInMixedArgumentTypes() {
        assertIsNotShellInjection("command arg1 'arg with spaces' arg2", " "); // Mixed argument types with internal spaces are safe
    }

    @Test
    void testIgnoresSpacesInCommandsWithConcatenatedArguments() {
        assertIsNotShellInjection("command 'arg1'arg2'arg3'", " "); // Lack of spaces in concatenated arguments is intentional and safe
    }

    @Test
    void testDoesNotFlagSpacesInCommandsWithNoArguments() {
        assertIsNotShellInjection("command", " "); // No arguments mean spaces are irrelevant
    }

    @Test
    void testConsidersSpacesInEnvironmentVariableAssignmentsAsSafe() {
        assertIsNotShellInjection("ENV_VAR='value' command", " "); // Spaces around environment variable assignments are not injections
    }
    @Test
    void testNewLinesInCommandsAreConsideredInjections() {
        assertIsShellInjection("ls \nrm", "\nrm");
        assertIsShellInjection("ls \nrm -rf", "\nrm -rf");
    }

    @Test
    void testNewLinesAloneAreNotConsideredInjections() {
        assertIsNotShellInjection("ls\n\n", "\n\n");
    }

    @Test
    void testCertainCommandsAreAlwaysFlaggedAsDangerous() {
        assertIsShellInjection("/bin/rm -rf", "/bin/rm -rf");
        assertIsShellInjection("rm -rf", "rm -rf");
        assertIsShellInjection("rm -rf /", "rm -rf /");
        assertIsShellInjection("sleep 10", "sleep 10");
        assertIsShellInjection("sleep 10 &", "sleep 10 &");
        assertIsShellInjection("shutdown -h now", "shutdown -h now");
        assertIsShellInjection("halt", "halt");
        assertIsShellInjection("poweroff", "poweroff");
        assertIsShellInjection("reboot", "reboot");
        assertIsShellInjection("reboot -f", "reboot -f");
        assertIsShellInjection("ifconfig", "ifconfig");
        assertIsShellInjection("ifconfig -a", "ifconfig -a");
        assertIsShellInjection("kill", "kill");
        assertIsShellInjection("killall", "killall");
        assertIsShellInjection("killall -9", "killall -9");
        assertIsShellInjection("chmod", "chmod");
        assertIsShellInjection("chmod 777", "chmod 777");
        assertIsShellInjection("chown", "chown");
        assertIsShellInjection("chown root", "chown root");
    }

    @Test
    void testRmBeingPartOfOtherCommands() {
        assertIsShellInjection("find /path/to/search -type f -name \"pattern\" | xargs rm", "rm");
        assertIsShellInjection("find /path/to/search -type f -name \"pattern\" -exec rm {} \\;", "rm");
        assertIsShellInjection("ls .|rm", "rm");
    }
    @Test
    void testIgnoresDangerousCommandsIfPartOfString() {
        assertIsNotShellInjection("binary sleepwithme", "sleepwithme");
        assertIsNotShellInjection("binary rm-rf", "rm-rf");
        assertIsNotShellInjection("term", "term");
        assertIsNotShellInjection("rm /files/rm.txt", "rm.txt");
    }

    @Test
    void testDoesNotFlagDomainNameAsArgumentUnlessContainsBackticks() {
        assertIsNotShellInjection("binary --domain www.example.com", "www.example.com");
        assertIsNotShellInjection("binary --domain https://www.example.com", "https://www.example.com");

        assertIsShellInjection("binary --domain www.example`whoami`.com", "www.example`whoami`.com");
        assertIsShellInjection("binary --domain https://www.example`whoami`.com", "https://www.example`whoami`.com");
    }

    @Test
    void testFlagsColonIfUsedAsCommand() {
        assertIsShellInjection(":|echo", ":|");
        assertIsShellInjection(":| echo", ":|");
        assertIsShellInjection(": | echo", ": |");
    }

    @Test
    void testDetectsShellInjection() {
        assertIsShellInjection("/usr/bin/kill", "/usr/bin/kill");
    }

    @Test
    void testDetectsShellInjectionWithUppercasePath() {
        assertIsShellInjection("/usr/bIn/kill", "/usr/bIn/kill");
    }

    @Test
    void testDetectsShellInjectionWithUppercaseCommand() {
        assertIsShellInjection("/bin/CAT", "/bin/CAT");
    }

    @Test
    void testDetectsShellInjectionWithUppercasePathAndCommand() {
        assertIsShellInjection("/bIn/LS -la", "/bIn/LS -la");
    }

    @Test
    void testShellInjectionWithMultipleSlashes() {
        assertIsShellInjection("//bin/ls", "//bin/ls");
        assertIsShellInjection("///bin/ls", "///bin/ls");
    }

    @Test
    void testShellInjectionWithDotDot() {
        assertIsShellInjection("../bin/ls", "../bin/ls");
        assertIsShellInjection("../../bin/ls", "../../bin/ls");
        assertIsShellInjection("/../bin/ls", "/../bin/ls");
        assertIsShellInjection("/./bin/ls", "/./bin/ls");
    }

    @Test
    void testShellInjectionWithTilde() {
        assertIsShellInjection("echo ~", "~");
        assertIsShellInjection("ls ~/.ssh", "~/.ssh");
    }

    @Test
    void testNoShellInjectionWithTilde() {
        assertIsNotShellInjection("~", "~");
        assertIsNotShellInjection("ls ~/path", "path");
    }

    @Test
    void testRealCase() {
        assertIsShellInjection(
                "command -disable-update-check -target https://examplx.com|curl+https://cde-123.abc.domain.com+%23 -json-export /tmp/5891/8526757.json -tags microsoft,windows,exchange,iis,gitlab,oracle,cisco,joomla -stats -stats-interval 3 -retries 3 -no-stdin",
                "https://examplx.com|curl+https://cde-123.abc.domain.com+%23"
        );
    }
    @Test
    void testFalsePositiveWithEmail() {
        assertIsNotShellInjection(
                "echo token | docker login --username john.doe@acme.com --password-stdin hub.acme.com",
                "john.doe@acme.com"
        );
    }

    @Test
    void testAtSignWithShellSyntax() {
        assertIsShellInjection("'echo \"${array[@]}\"'", "${array[@]}");
        assertIsShellInjection("echo $@", "$@");
    }

    @Test
    void testAllowsCommaSeparatedList() {
        assertIsNotShellInjection(
                "command -tags php,laravel,drupal,phpmyadmin,symfony -stats",
                "php,laravel,drupal,phpmyadmin,symfony"
        );
    }

    @Test
    void testItFlagsCommaInLoop() {
        assertIsShellInjection(
                "command for (( i=0, j=10; i<j; i++, j-- ))\n" +
                        "do\n" +
                        "    echo \"$i $j\"\n" +
                        "done",
                "for (( i=0, j=10; i<j; i++, j-- ))"
        );
    }
}
