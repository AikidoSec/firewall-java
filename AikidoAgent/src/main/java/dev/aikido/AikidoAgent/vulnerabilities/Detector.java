package dev.aikido.AikidoAgent.vulnerabilities;

import java.util.Map;

public interface Detector {
    DetectorResult run(String userInput, String[] arguments);
    public static class DetectorResult {
        private final boolean detectedAttack;
        private final Map<String, String> metadata;
        public DetectorResult(boolean detectedAttack, Map<String, String> metadata) {
            this.detectedAttack = detectedAttack;
            this.metadata = metadata;
        }
        public DetectorResult() {
            this.detectedAttack = false;
            this.metadata = null;
        }

        public boolean isDetectedAttack() {
            return detectedAttack;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
}
