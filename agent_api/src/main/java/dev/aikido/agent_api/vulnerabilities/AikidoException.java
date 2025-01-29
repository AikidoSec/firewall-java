package dev.aikido.agent_api.vulnerabilities;

public class AikidoException extends RuntimeException {
    public AikidoException(String msg) {
        super(msg);
    }

    public AikidoException() {
        super("Aikido Zen has blocked an unknown vulnerability");
    }

    public static String generateDefaultMessage(String vulnerabilityName) {
        return "Aikido Zen has blocked " + vulnerabilityName;
    }
}
