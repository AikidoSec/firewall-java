package helpers;

import dev.aikido.agent_api.helpers.env.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    // Test for null token
    @Test
    void constructor_shouldThrowError_whenTokenIsNull() {
        Executable executable = () -> new Token(null);
        assertThrows(Error.class, executable, "Token cannot be null");
    }

    // Test for empty token
    @Test
    void constructor_shouldThrowError_whenTokenIsEmpty() {
        Executable executable = () -> new Token("");
        assertThrows(Error.class, executable, "Token cannot be empty");
    }

    // Test for valid token retrieval
    @Test
    void get_shouldReturnToken() {
        String expectedToken = "myToken";
        Token token = new Token(expectedToken);
        assertEquals(expectedToken, token.get());
    }

    // Test for creating token from environment variable
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "envToken")
    @Test
    void fromEnv_shouldReturnToken_whenEnvironmentVariableIsSet() {
        Token token = Token.fromEnv();
        assertEquals("envToken", token.get());
    }

    // Test empty token from ENV:
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "")
    @Test
    void fromEnv_emptyToken() {
        Token token = Token.fromEnv();
        assertNull(token);
    }

    // Test null token from ENV:
    @ClearEnvironmentVariable(key = "AIKIDO_TOKEN")
    @Test
    void fromEnv_nullToken() {
        Token token = Token.fromEnv();
        assertNull(token);
    }

    // Test for hashing a valid token
    @Test
    void hash_shouldReturnHashedToken() {
        String tokenValue = "myToken";
        Token token = new Token(tokenValue);
        String expectedHash = "bea25019538dda1939a7d04c1af0a332b1147a6c09403587ccfb9a029ff1bfdc";
        assertEquals(expectedHash, token.hash());
    }

    @Test
    void hash_shouldReturnDefault_whenNoSuchAlgorithmExceptionOccurs() {
        Token token = new Token("myToken");
        assertEquals("bea25019538dda1939a7d04c1af0a332b1147a6c09403587ccfb9a029ff1bfdc", token.hash());
    }

    // Test for token with special characters
    @Test
    void constructor_shouldAcceptTokenWithSpecialCharacters() {
        String specialToken = "myToken!@#";
        Token token = new Token(specialToken);
        assertEquals(specialToken, token.get());
    }

    // Test for token with whitespace
    @Test
    void constructor_shouldAcceptTokenWithLeadingAndTrailingWhitespace() {
        String tokenWithWhitespace = "  myToken  ";
        Token token = new Token(tokenWithWhitespace.trim());
        assertEquals(tokenWithWhitespace.trim(), token.get());
    }

    // Test for token with maximum length
    @Test
    void constructor_shouldAcceptTokenWithMaximumLength() {
        String maxLengthToken = "a".repeat(1024); // Assuming 1024 is the max length
        Token token = new Token(maxLengthToken);
        assertEquals(maxLengthToken, token.get());
    }

    // Test for token with minimum length
    @Test
    void constructor_shouldAcceptTokenWithMinimumLength() {
        String minLengthToken = "a"; // Assuming 1 is the minimum length
        Token token = new Token(minLengthToken);
        assertEquals(minLengthToken, token.get());
    }

    // Test for hash consistency
    @Test
    void hash_shouldReturnSameHashForSameToken() {
        String tokenValue = "consistentToken";
        Token token1 = new Token(tokenValue);
        Token token2 = new Token(tokenValue);
        assertEquals(token1.hash(), token2.hash());
    }

    // Test for different tokens producing different hashes
    @Test
    void hash_shouldReturnDifferentHashesForDifferentTokens() {
        Token token1 = new Token("token1");
        Token token2 = new Token("token2");
        assertNotEquals(token1.hash(), token2.hash());
    }

    // Test for token with numeric characters
    @Test
    void constructor_shouldAcceptTokenWithNumericCharacters() {
        String numericToken = "1234567890";
        Token token = new Token(numericToken);
        assertEquals(numericToken, token.get());
    }

    // Test for token with mixed case
    @Test
    void constructor_shouldAcceptTokenWithMixedCase() {
        String mixedCaseToken = "MyToken123";
        Token token = new Token(mixedCaseToken);
        assertEquals(mixedCaseToken, token.get());
    }
}
