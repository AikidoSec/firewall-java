package dev.aikido.agent_api.vulnerabilities.shell_injection;

import dev.aikido.agent_api.vulnerabilities.AikidoException;

public class ShellInjectionException extends AikidoException {
    public ShellInjectionException(String msg) {
        super(msg);
    }

    public static ShellInjectionException get() {
        String defaultMsg = generateDefaultMessage("Shell Injection");
        return new ShellInjectionException(defaultMsg);
    }
}
