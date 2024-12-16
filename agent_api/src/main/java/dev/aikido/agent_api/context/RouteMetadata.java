package dev.aikido.agent_api.context;

import java.io.Serializable;

public record RouteMetadata(String route, String url, String method) implements Serializable {
}
