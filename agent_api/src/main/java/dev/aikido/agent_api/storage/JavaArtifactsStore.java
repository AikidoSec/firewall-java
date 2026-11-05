package dev.aikido.agent_api.storage;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class JavaArtifactsStore {
    private static final ConcurrentMap<String, JavaArtifact> ARTIFACTS = new ConcurrentHashMap<>();

    private JavaArtifactsStore() {}

    public static void add(JavaArtifact artifact) {
        ARTIFACTS.putIfAbsent(artifact.sha1(), artifact);
    }

    public static List<JavaArtifact> getArtifactsAsList() {
        return ARTIFACTS.values().stream()
                .sorted(Comparator.comparing(JavaArtifact::sha1))
                .toList();
    }

    public static void clear() {
        ARTIFACTS.clear();
    }
}
