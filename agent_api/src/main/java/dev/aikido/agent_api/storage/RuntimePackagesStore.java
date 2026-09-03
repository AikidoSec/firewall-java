package dev.aikido.agent_api.storage;

import java.util.Comparator;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RuntimePackagesStore {
    private static final ConcurrentMap<String, RuntimePackage> PACKAGES = new ConcurrentHashMap<>();

    private RuntimePackagesStore() {}

    public static void addAll(Collection<RuntimePackage> packages) {
        for (RuntimePackage pkg : packages) {
            PACKAGES.putIfAbsent(packageKey(pkg), pkg);
        }
    }

    public static List<RuntimePackage> getPackagesAsList() {
        return PACKAGES.values().stream()
                .sorted(
                        Comparator.comparing(RuntimePackage::name)
                                .thenComparing(RuntimePackage::version)
                )
                .toList();
    }

    public static void clear() {
        PACKAGES.clear();
    }

    private static String packageKey(RuntimePackage pkg) {
        return pkg.name() + '\0' + pkg.version();
    }
}
