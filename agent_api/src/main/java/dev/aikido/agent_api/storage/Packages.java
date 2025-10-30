package dev.aikido.agent_api.storage;

import java.util.*;

public class Packages {
    public record PackageInfo(String name, String version, long requiredAt) {}
    private final Map<String, List<PackageInfo>> packages = new HashMap<>();

    public void addPackage(String name, String version) {
        List<PackageInfo> versions = packages.computeIfAbsent(name, k -> new ArrayList<>());
        for (PackageInfo pkg : versions) {
            if (pkg.version().equals(version)) {
                return;
            }
        }
        versions.add(new PackageInfo(name, version, System.currentTimeMillis()));
    }

    public List<PackageInfo> asArray() {
        List<PackageInfo> result = new ArrayList<>();
        for (List<PackageInfo> versionList : packages.values()) {
            result.addAll(versionList);
        }
        return result;
    }

    public void clear() {
        packages.clear();
    }
}

