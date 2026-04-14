package dev.aikido.agent_api.vulnerabilities;

public class DangerousBodyException extends AikidoException {
    public DangerousBodyException(String reason) {
        super(generateDefaultMessage("Dangerous Body") + ": " + reason);
    }

    public static DangerousBodyException jwtTooLarge() {
        return new DangerousBodyException("JWT payload too large");
    }

    public static DangerousBodyException bodyTooDeep() {
        return new DangerousBodyException("Body is too deeply nested to scan");
    }
}
