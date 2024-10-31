package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.vulnerabilities.AikidoException;
import dev.aikido.agent_api.vulnerabilities.sql_injection.Dialect;

public class SSRFException extends AikidoException {
    public SSRFException(String msg) {
        super(msg);
    }

    public static dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException get() {
        String defaultMsg = generateDefaultMessage("a server-side request forgery");
        return new dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException(defaultMsg);
    }
}
