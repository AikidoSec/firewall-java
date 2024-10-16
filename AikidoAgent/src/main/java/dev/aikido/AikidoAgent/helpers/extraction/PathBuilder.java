package dev.aikido.AikidoAgent.helpers.extraction;

import java.util.List;

public class PathBuilder {
    public static String buildPathToPayload(List<PathPart> pathToPayload) {
        if (pathToPayload.isEmpty()) {
            return ".";
        }

        StringBuilder result = new StringBuilder();
        for (PathPart part : pathToPayload) {
            if ("object".equals(part.getType())) {
                result.append(".").append(part.getKey());
            } else if ("array".equals(part.getType())) {
                result.append(".[").append(part.getIndex()).append("]");
            } else if ("jwt".equals(part.getType())) {
                result.append("<jwt>");
            }
        }

        return result.toString();
    }
    public static class PathPart {
        private final String type;
        private String key; // Only used if type is "object"
        private int index;  // Only used if type is "array"

        // Constructor for object type
        public PathPart(String type, String key) {
            this.type = type;
            this.key = key;
        }

        // Constructor for array type
        public PathPart(String type, int index) {
            this.type = type;
            this.index = index;
        }

        // Constructor for JWT type
        public PathPart(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String getKey() {
            return key;
        }

        public int getIndex() {
            return index;
        }
    }
}

