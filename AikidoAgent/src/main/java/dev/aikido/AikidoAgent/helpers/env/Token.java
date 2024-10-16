package dev.aikido.AikidoAgent.helpers.env;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Token {
    private final String token;
    public Token(String token) {
        if (token == null) {
            throw new Error("Token cannot be null");
        }
        if (token.isEmpty()) {
            throw new Error("Token cannot be empty");
        }
        this.token = token;
    }
    public String get() {
        return token;
    }
    public static Token fromEnv() {
        return new Token(System.getenv("AIKIDO_TOKEN"));
    }
    public String hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes());
            return Arrays.toString(hashBytes);
        } catch (NoSuchAlgorithmException ignored) {
        }
        return "default";
    }
}
