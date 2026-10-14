package dev.aikido.agent_api.helpers.packages;

import dev.aikido.agent_api.storage.RuntimePackagesStore;

import java.security.ProtectionDomain;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public final class RuntimePackageCollector {
    private static final BlockingQueue<ObservedLocation> PENDING_LOCATIONS = new LinkedBlockingQueue<>();
    private static final Set<String> OBSERVED_LOCATIONS = ConcurrentHashMap.newKeySet();

    private RuntimePackageCollector() {}

    public static void start() {
        Thread worker = new Thread(RuntimePackageCollector::processLocations, "aikido-package-scanner");
        worker.setDaemon(true);
        worker.start();
    }

    public static void observeClass(String className, ProtectionDomain protectionDomain) {
        if (className == null || className.startsWith("dev/aikido/") || className.startsWith("dev.aikido.")) {
            return;
        }
        try {
            if (protectionDomain == null || protectionDomain.getCodeSource() == null) {
                return;
            }
            String location = protectionDomain.getCodeSource().getLocation().toString();
            if (isAgentLocation(location)) {
                return;
            }
            String locationKey = JarPackageScanner.getJarLocationKey(location);
            if (locationKey != null && OBSERVED_LOCATIONS.add(locationKey)) {
                PENDING_LOCATIONS.add(new ObservedLocation(location, System.currentTimeMillis()));
            }
        } catch (Throwable ignored) {
            // Package reporting must never interfere with application class loading.
        }
    }

    private static void processLocations() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ObservedLocation location = PENDING_LOCATIONS.take();
                RuntimePackagesStore.addAll(JarPackageScanner.findMavenPackages(location.url(), location.requiredAt()));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } catch (Throwable ignored) {
                // A malformed or inaccessible JAR must not stop future package discovery.
            }
        }
    }

    private static boolean isAgentLocation(String location) {
        String agentDirectory = System.getProperty("AIK_agent_dir");
        if (agentDirectory == null) {
            return false;
        }
        String directoryUrl = new java.io.File(agentDirectory).toURI().toString();
        return isAgentJar(location, directoryUrl);
    }

    private static boolean isAgentJar(String url, String directoryUrl) {
        return url.startsWith(directoryUrl + "agent.jar") || url.startsWith(directoryUrl + "agent_api.jar");
    }

    private record ObservedLocation(String url, long requiredAt) {}
}
