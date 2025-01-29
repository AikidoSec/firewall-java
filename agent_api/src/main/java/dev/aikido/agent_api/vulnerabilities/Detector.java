package dev.aikido.agent_api.vulnerabilities;

import java.util.Map;

public interface Detector {
    DetectorResult run(String userInput, String[] arguments);

    /**
     * To increase performance, we allow vulnerability algorithms to implement an early return
     * this means that we do not loop over all user input in the request if the data we are analyzing
     * is not harmful (regardless of user input)
     */
    default boolean returnEarly(String[] args) {
        return false;
    }

    public static class DetectorResult {
        private final boolean detectedAttack;
        private final Map<String, String> metadata;
        private final AikidoException exception;

        public DetectorResult(boolean detectedAttack, Map<String, String> metadata, AikidoException exception) {
            this.detectedAttack = detectedAttack;
            this.metadata = metadata;
            this.exception = exception;
        }

        public DetectorResult() {
            this.detectedAttack = false;
            this.metadata = null;
            this.exception = null;
        }

        public boolean isDetectedAttack() {
            return detectedAttack;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public AikidoException getException() {
            return this.exception;
        }
    }
}
