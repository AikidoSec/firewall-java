package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.vulnerabilities.AikidoException;

public class StoredSSRFException extends AikidoException {

    public static StoredSSRFException get() {
        String defaultMsg = generateDefaultMessage("a stored server-side request forgery");
        return new StoredSSRFException(defaultMsg);
    }
}
