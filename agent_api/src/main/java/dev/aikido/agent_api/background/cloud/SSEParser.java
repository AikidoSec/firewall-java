package dev.aikido.agent_api.background.cloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

public class SSEParser {
    private final BufferedReader reader;

    public SSEParser(BufferedReader reader) {
        this.reader = reader;
    }

    public record Event(String event, String data) {}

    public Optional<Event> nextEvent() throws IOException {
        String eventType = "";
        StringBuilder data = new StringBuilder();
        boolean hasData = false;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (hasData) {
                    if (data.charAt(data.length() - 1) == '\n') {
                        data.setLength(data.length() - 1);
                    }
                    return Optional.of(new Event(eventType, data.toString()));
                }
                eventType = "";
                data.setLength(0);
                continue;
            }
            if (line.startsWith(":")) {
                continue;
            }

            int sep = line.indexOf(':');
            String field = sep == -1 ? line : line.substring(0, sep);
            String value;
            if (sep == -1) {
                value = "";
            } else {
                value = line.substring(sep + 1);
                if (value.startsWith(" ")) {
                    value = value.substring(1);
                }
            }

            if (field.equals("event")) {
                eventType = value;
            } else if (field.equals("data")) {
                data.append(value).append("\n");
                hasData = true;
            }
        }
        return Optional.empty();
    }
}
