package dev.aikido.AikidoAgent.vulnerabilities;

public interface Detector {
    boolean run(String userInput, String[] arguments);
}
