package dev.aikido.AikidoAgent.helpers.env;

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
}
