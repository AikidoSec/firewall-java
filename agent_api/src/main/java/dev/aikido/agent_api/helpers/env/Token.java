package dev.aikido.agent_api.helpers.env;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        String token = System.getenv("AIKIDO_TOKEN");
        if (token == null || token.isEmpty()) {
            return null;
        }
        return new Token(token);
    }

    /**
     * Extracts the region from a runtime token.
     * New format: AIK_RUNTIME_{sys_group_id}_{service_id}_{region}_{random}
     * Old format: AIK_RUNTIME_{sys_group_id}_{service_id}_{random}
     */
    public static String extractRegionFromToken(String token) {
        if (token == null || !token.startsWith("AIK_RUNTIME_")) {
            return "EU";
        }
        String tokenWithoutPrefix = token.substring("AIK_RUNTIME_".length());
        String[] parts = tokenWithoutPrefix.split("_");
        if (parts.length == 4) {
            return parts[2];
        }
        return "EU";
    }

    public String getRegion() {
        return extractRegionFromToken(token);
    }

    /**
     * Hashes the token with SHA-256 and returns the hashed bytes in a hex representation (alphanum)
     */
    public String hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes());

            // Convert hashed bytes to hexadecimal string :
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Append leading zero for single digit
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ignored) {
        }
        return "default";
    }
}
