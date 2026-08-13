package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.helpers.packages.JarPackageScanner;

import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RuntimePackagesStore {
    private static final int MAX_PENDING_LOCATIONS = 1_024;
    private static final BlockingQueue<ObservedLocation> PENDING_LOCATIONS =
            new ArrayBlockingQueue<>(MAX_PENDING_LOCATIONS);
    private static final Set<String> OBSERVED_LOCATIONS = ConcurrentHashMap.newKeySet();
    private static final ConcurrentMap<String, RuntimePackage> PACKAGES = new ConcurrentHashMap<>();

    static {
        Thread worker = new Thread(RuntimePackagesStore::processLocations, "aikido-package-scanner");
        worker.setDaemon(true);
        worker.start();
    }

    private RuntimePackagesStore() {}

    public static void observeClass(
            String className,
            ClassLoader loader,
            ProtectionDomain protectionDomain
    ) {
        try {
            URL codeSource = getCodeSource(protectionDomain);
            String resourceName = className.replace('.', '/') + ".class";
            URL resource = getResource(loader, resourceName);
            if (resource == null) {
                return;
            }
            String resourceUrl = resource.toString();

            if (isAgentLocation(resourceUrl, codeSource)) {
                return;
            }
            String locationKey = JarPackageScanner.getJarLocationKey(resourceUrl);
            if (locationKey == null || !OBSERVED_LOCATIONS.add(locationKey)) {
                return;
            }
            ObservedLocation location = new ObservedLocation(resourceUrl, System.currentTimeMillis());
            if (!PENDING_LOCATIONS.offer(location)) {
                OBSERVED_LOCATIONS.remove(locationKey);
            }
        } catch (Throwable ignored) {
            // Package reporting must never interfere with application class loading.
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

    private static void processLocations() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ObservedLocation location = PENDING_LOCATIONS.take();
                List<RuntimePackage> packages = JarPackageScanner.findMavenPackages(location.url(), location.requiredAt());
                for (RuntimePackage pkg : packages) {
                    PACKAGES.putIfAbsent(packageKey(pkg), pkg);
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } catch (Throwable ignored) {
                // A malformed or inaccessible JAR must not stop future package discovery.
            }
        }
    }

    private static URL getCodeSource(ProtectionDomain protectionDomain) {
        if (protectionDomain == null || protectionDomain.getCodeSource() == null) {
            return null;
        }
        return protectionDomain.getCodeSource().getLocation();
    }

    private static URL getResource(ClassLoader loader, String resourceName) {
        if (loader == null) {
            return ClassLoader.getSystemResource(resourceName);
        }
        return loader.getResource(resourceName);
    }

    private static boolean isAgentLocation(String resource, URL codeSource) {
        String agentDirectory = System.getProperty("AIK_agent_dir");
        if (agentDirectory == null) {
            return false;
        }
        String directoryUrl = new java.io.File(agentDirectory).toURI().toString();
        String sourceUrl = "";
        if (codeSource != null) {
            sourceUrl = codeSource.toString();
        }
        if (isAgentJar(sourceUrl, directoryUrl)) {
            return true;
        }
        return isAgentJar(resource.replaceFirst("^jar:", ""), directoryUrl);
    }

    private static boolean isAgentJar(String url, String directoryUrl) {
        return url.startsWith(directoryUrl + "agent.jar") || url.startsWith(directoryUrl + "agent_api.jar");
    }

    private static String packageKey(RuntimePackage pkg) {
        return pkg.name() + '\0' + pkg.version();
    }

    private record ObservedLocation(String url, long requiredAt) {}
}
