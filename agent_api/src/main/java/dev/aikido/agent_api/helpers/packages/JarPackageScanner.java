package dev.aikido.agent_api.helpers.packages;

import dev.aikido.agent_api.storage.RuntimePackage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public final class JarPackageScanner {
    private static final int MAX_METADATA_BYTES = 1024 * 1024;
    private static final Pattern MAVEN_PACKAGE_NAME = Pattern.compile("[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+");

    private JarPackageScanner() {}

    public static List<RuntimePackage> findMavenPackages(
            String classResourceUrl,
            long requiredAt
    ) {
        try {
            JarLocation location = JarLocation.parse(classResourceUrl);
            if (location == null) {
                return List.of();
            }
            if (location.nestedEntry() == null) {
                try (InputStream input = new BufferedInputStream(Files.newInputStream(location.outerJar()))) {
                    return findMavenPackages(input, requiredAt);
                }
            }
            try (JarFile outerJar = new JarFile(location.outerJar().toFile())) {
                JarEntry nestedJar = outerJar.getJarEntry(location.nestedEntry());
                if (nestedJar == null) {
                    return List.of();
                }
                try (InputStream input = new BufferedInputStream(outerJar.getInputStream(nestedJar))) {
                    return findMavenPackages(input, requiredAt);
                }
            }
        } catch (IOException | RuntimeException ignored) {
            return List.of();
        }
    }

    public static String getJarLocationKey(String classResourceUrl) {
        try {
            JarLocation location = JarLocation.parse(classResourceUrl);
            if (location == null) {
                return null;
            }
            return location.getKey();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static List<RuntimePackage> findMavenPackages(
            InputStream input,
            long requiredAt
    ) throws IOException {
        Map<String, RuntimePackage> packages = new LinkedHashMap<>();

        try (JarInputStream jar = new JarInputStream(input)) {
            JarEntry entry;
            while ((entry = jar.getNextJarEntry()) != null) {
                if (!entry.isDirectory() && isPomProperties(entry.getName())) {
                    byte[] metadata = jar.readNBytes(MAX_METADATA_BYTES + 1);
                    if (metadata.length <= MAX_METADATA_BYTES) {
                        addMavenPackage(metadata, requiredAt, packages);
                    }
                }
            }
        }

        return packages.values().stream()
                .sorted(
                        Comparator.comparing(RuntimePackage::name)
                                .thenComparing(RuntimePackage::version)
                )
                .toList();
    }

    private static boolean isPomProperties(String name) {
        return name.startsWith("META-INF/maven/") && name.endsWith("/pom.properties");
    }

    private static void addMavenPackage(
            byte[] metadata,
            long requiredAt,
            Map<String, RuntimePackage> packages
    ) {
        Properties properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(metadata));
        } catch (IOException | IllegalArgumentException ignored) {
            return;
        }
        String groupId = clean(properties.getProperty("groupId"));
        String artifactId = clean(properties.getProperty("artifactId"));
        String version = clean(properties.getProperty("version"));
        if (groupId == null || artifactId == null || version == null) {
            return;
        }
        String packageName = groupId + ":" + artifactId;
        if (!MAVEN_PACKAGE_NAME.matcher(packageName).matches()) {
            return;
        }
        add(packageName, version, requiredAt, packages);
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static void add(
            String name,
            String version,
            long requiredAt,
            Map<String, RuntimePackage> packages
    ) {
        packages.putIfAbsent(name + '\0' + version, new RuntimePackage(name, version, requiredAt));
    }

    private record JarLocation(Path outerJar, String nestedEntry) {
        private static JarLocation parse(String url) {
            if (url == null) {
                return null;
            }
            String value = url;
            if (value.startsWith("jar:")) {
                value = value.substring(4);
            }
            if (value.startsWith("nested:")) {
                value = value.substring(7);
            }
            String lowerCaseValue = value.toLowerCase(Locale.ROOT);
            int outerEnd = lowerCaseValue.indexOf(".jar!/");
            int springBootOuterEnd = lowerCaseValue.indexOf(".jar/!");
            if (outerEnd < 0 || springBootOuterEnd >= 0 && springBootOuterEnd < outerEnd) {
                outerEnd = springBootOuterEnd;
            }
            if (outerEnd < 0) {
                int jarEnd = lowerCaseValue.indexOf(".jar");
                if (jarEnd < 0) {
                    return null;
                }
                Path jar = toPath(value.substring(0, jarEnd + 4));
                return new JarLocation(jar, null);
            }

            Path outerJar = toPath(value.substring(0, outerEnd + 4));
            int nestedStart = outerEnd + 6;
            int nestedEnd = lowerCaseValue.indexOf(".jar!/", nestedStart);
            if (nestedEnd < 0 && lowerCaseValue.endsWith(".jar")) {
                nestedEnd = value.length() - 4;
            }
            String nestedEntry = null;
            if (nestedEnd >= 0) {
                nestedEntry = value.substring(nestedStart, nestedEnd + 4);
            }
            return new JarLocation(outerJar, nestedEntry);
        }

        private static Path toPath(String value) {
            if (value.startsWith("file:")) {
                return Path.of(URI.create(value));
            }
            return Path.of(value);
        }

        private String getKey() {
            String key = outerJar.toAbsolutePath().normalize().toString();
            if (nestedEntry != null) {
                key += "!/" + nestedEntry;
            }
            return key;
        }
    }
}
